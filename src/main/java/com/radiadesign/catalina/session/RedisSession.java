package com.radiadesign.catalina.session;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;


public class RedisSession extends StandardSession {
	protected static Boolean manualDirtyTrackingSupportEnabled = false;

	public static void setManualDirtyTrackingSupportEnabled(Boolean enabled) {
		manualDirtyTrackingSupportEnabled = enabled;
	}

	protected static String manualDirtyTrackingAttributeKey = "__changed__";

	public static void setManualDirtyTrackingAttributeKey(String key) {
		manualDirtyTrackingAttributeKey = key;
	}

	protected HashMap<String, Object> changedAttributes;
	protected Boolean dirty;

	public RedisSession(Manager manager) {
		super(manager);
		resetDirtyTracking();
	}

	public Boolean isDirty() {
		return dirty || !changedAttributes.isEmpty();
	}

	public HashMap<String, Object> getChangedAttributes() {
		return changedAttributes;
	}

	public void resetDirtyTracking() {
		changedAttributes = new HashMap<String, Object>();
		dirty = false;
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (manualDirtyTrackingSupportEnabled && manualDirtyTrackingAttributeKey.equals(key)) {
			dirty = true;
			return;
		}

		Object oldValue = getAttribute(key);
		if ((value != null || oldValue != null)
				&& (value == null && oldValue != null || oldValue == null && value != null
						|| !value.getClass().isInstance(oldValue) || !value.equals(oldValue))) {
			changedAttributes.put(key, value);
		}

		super.setAttribute(key, value);
	}

	@Override
	public void removeAttribute(String name) {
		dirty = true;
		super.removeAttribute(name);
	}

	@Override
	public void setId(String id) {
		// Specifically do not call super(): it's implementation does unexpected
		// things
		// like calling manager.remove(session.id) and manager.add(session).

		this.id = id;
	}

	@Override
	public void setPrincipal(Principal principal) {
		dirty = true;
		super.setPrincipal(principal);
	}

	/**
	 * Read a serialized version of this session object from the specified
	 * object input stream.
	 * <p>
	 * <b>IMPLEMENTATION NOTE</b>: The reference to the owning Manager is not
	 * restored by this method, and must be set explicitly.
	 * 
	 * @param stream
	 *            The input stream to read from
	 * 
	 * @exception ClassNotFoundException
	 *                if an unknown class is specified
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	protected void doReadObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {

		// Deserialize the scalar instance variables (except Manager)
		authType = null; // Transient only
		creationTime = ((Long) stream.readObject()).longValue();
		lastAccessedTime = ((Long) stream.readObject()).longValue();
		maxInactiveInterval = ((Integer) stream.readObject()).intValue();
		isNew = ((Boolean) stream.readObject()).booleanValue();
		isValid = ((Boolean) stream.readObject()).booleanValue();
		thisAccessedTime = ((Long) stream.readObject()).longValue();
		principal = null; // Transient only
		// setId((String) stream.readObject());
		id = (String) stream.readObject();
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("readObject() loading session " + id);

		// Deserialize the attribute count and attribute values
		if (attributes == null)
			attributes = new ConcurrentHashMap<>();
		int n = ((Integer) stream.readObject()).intValue();
		boolean isValidSave = isValid;
		isValid = true;
		for (int i = 0; i < n; i++) {
			String name = (String) stream.readObject();
			Object value = stream.readObject();
			if ((value instanceof String) && (value.equals(NOT_SERIALIZED)))
				continue;
			if (manager.getContext().getLogger().isDebugEnabled())
				manager.getContext().getLogger().debug("  loading attribute '" + name + "' with value '" + value + "'");
			attributes.put(name, value);
		}
		isValid = isValidSave;

		if (listeners == null) {
			listeners = new ArrayList<>();
		}

		if (notes == null) {
			notes = new Hashtable<>();
		}
	}

	/**
	 * Write a serialized version of this session object to the specified object
	 * output stream.
	 * <p>
	 * <b>IMPLEMENTATION NOTE</b>: The owning Manager will not be stored in the
	 * serialized representation of this Session. After calling
	 * <code>readObject()</code>, you must set the associated Manager
	 * explicitly.
	 * <p>
	 * <b>IMPLEMENTATION NOTE</b>: Any attribute that is not Serializable will
	 * be unbound from the session, with appropriate actions if it implements
	 * HttpSessionBindingListener. If you do not want any such attributes, be
	 * sure the <code>distributable</code> property of the associated Manager is
	 * set to <code>true</code>.
	 * 
	 * @param stream
	 *            The output stream to write to
	 * 
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	protected void doWriteObject(ObjectOutputStream stream) throws IOException {

		// Write the scalar instance variables (except Manager)
		stream.writeObject(Long.valueOf(creationTime));
		stream.writeObject(Long.valueOf(lastAccessedTime));
		stream.writeObject(Integer.valueOf(maxInactiveInterval));
		stream.writeObject(Boolean.valueOf(isNew));
		stream.writeObject(Boolean.valueOf(isValid));
		stream.writeObject(Long.valueOf(thisAccessedTime));
		stream.writeObject(id);
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("writeObject() storing session " + id);

		// Accumulate the names of serializable and non-serializable attributes
		String keys[] = keys();
		ArrayList<String> saveNames = new ArrayList<>();
		ArrayList<Object> saveValues = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			Object value = attributes.get(keys[i]);
			if (value == null)
				continue;
			else if ((value instanceof Serializable) && (!exclude(keys[i]))) {
				saveNames.add(keys[i]);
				saveValues.add(value);
			} else {
				removeAttributeInternal(keys[i], true);
			}
		}

		// Serialize the attribute count and the Serializable attributes
		int n = saveNames.size();
		stream.writeObject(Integer.valueOf(n));
		for (int i = 0; i < n; i++) {
			stream.writeObject(saveNames.get(i));
			try {
				stream.writeObject(saveValues.get(i));
				if (manager.getContext().getLogger().isDebugEnabled())
					manager.getContext().getLogger().debug(
							"  storing attribute '" + saveNames.get(i) + "' with value '" + saveValues.get(i) + "'");
			} catch (NotSerializableException e) {
				manager.getContext().getLogger()
						.warn(sm.getString("standardSession.notSerializable", saveNames.get(i), id), e);
				stream.writeObject(NOT_SERIALIZED);
				if (manager.getContext().getLogger().isDebugEnabled())
					manager.getContext().getLogger()
							.debug("  storing attribute '" + saveNames.get(i) + "' with value NOT_SERIALIZED");
			}
		}

	}

	/**
	 * 从session对象中读入数据
	 * 
	 * @param userinfo
	 */
	public void readObjectFromSessionLoginUserInfo(SessionLoginUserInfo userinfo) {
		// Deserialize the scalar instance variables (except Manager)
		authType = null; // Transient only
		long creationTime2 = userinfo.getCreationTime();
		if (creationTime2 == 0) {
			creationTime2 = System.currentTimeMillis();
			userinfo.setCreationTime(creationTime2);
		}
		creationTime = (Long) creationTime2;
		// lastAccessedTime = ((Long) stream.readObject()).longValue();
		maxInactiveInterval = userinfo.getUser_auth().getExpireIn().intValue();
		isNew = userinfo.isNew();
		isValid = userinfo.isValid();
		thisAccessedTime = (Long) System.currentTimeMillis();
		principal = null; // Transient only
		// setId((String) stream.readObject());
		id = (String) userinfo.getId();
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("readObject() loading session " + id);

		// Deserialize the attribute count and attribute values
		if (attributes == null)
			attributes = new ConcurrentHashMap<>();
		boolean isValidSave = isValid;
		isValid = true;
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger()
					.debug("put loginUser attribute  with value '" + userinfo.getUser_auth() + "'");
		attributes.put("loginUser", userinfo.getUser_auth());
		Map<String, Object> attrs = userinfo.getAttrs();
		for (Iterator iterator = attrs.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Object> en = (Map.Entry<String, Object>) iterator.next();
			String name = en.getKey();
			Object value = en.getValue();
			if ((value instanceof String) && (value.equals(NOT_SERIALIZED)))
				continue;
			if (manager.getContext().getLogger().isDebugEnabled())
				manager.getContext().getLogger().debug("  loading attribute '" + name + "' with value '" + value + "'");
			attributes.put(name, value);
		}
		isValid = isValidSave;

		if (listeners == null) {
			listeners = new ArrayList<>();
		}

		if (notes == null) {
			notes = new Hashtable<>();
		}

	}

	/**
	 * 把session信息写成SessionLoginUserInfo 对象
	 * 
	 * @return
	 * @throws IOException
	 */
	public SessionLoginUserInfo doWriteObject() throws IOException {

		SessionLoginUserInfo userinfo = new SessionLoginUserInfo();
		userinfo.setCreationTime(creationTime);
		userinfo.setId(id);
		userinfo.setNew(isNew);
		userinfo.setValid(isValid);
		userinfo.setUser_auth((SessionUserAuthInfo) attributes.get("loginUser"));
		userinfo.setThisAccessedTime(thisAccessedTime);
		//写入其它的信息
		String keys[] = keys();
		ArrayList<String> saveNames = new ArrayList<>();
		ArrayList<Object> saveValues = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			if("loginUser".equals(keys[i])){
				//此部分信息不需要再保存一次
				continue;
			}
			Object value = attributes.get(keys[i]);
			
			if (value == null)
				continue;
			else if ((value instanceof Serializable) && (!exclude(keys[i]))) {
				saveNames.add(keys[i]);
				saveValues.add(value);
			} else {
				removeAttributeInternal(keys[i], true);
			}
		}
		int n = saveNames.size();
		for (int i = 0; i < n; i++) {
				userinfo.putAttr(saveNames.get(i), saveValues.get(i));
				if (manager.getContext().getLogger().isDebugEnabled())
					manager.getContext().getLogger().debug(
							"  storing attribute '" + saveNames.get(i) + "' with value '" + saveValues.get(i) + "'");
		}
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("writeObject() storing session " + id);

		return userinfo;

	}

}
