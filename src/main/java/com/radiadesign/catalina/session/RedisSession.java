package com.radiadesign.catalina.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


public class RedisSession extends StandardSession {
	
	private final Log log = LogFactory.getLog(RedisSessionManager.class);
	
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
	/**
	 * 存放登录用户的信息的key
	 */
	private String redisSessionAttrKey;
	
	public RedisSession(Manager manager) {
		super(manager);
		resetDirtyTracking();
		if (manager instanceof RedisSessionManager) {
			RedisSessionManager ma = (RedisSessionManager) manager;
			redisSessionAttrKey=ma.getLoginUserAttrKey();
			if(log.isTraceEnabled()){
				log.trace("  loginuser attr key= " + redisSessionAttrKey);
			}
			System.out.println("  loginuser attr key= " + redisSessionAttrKey);
		}
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
		System.out.println("setAttribute,key="+key+",value="+value);
		if (manualDirtyTrackingSupportEnabled && manualDirtyTrackingAttributeKey.equals(key)) {
			dirty = true;
//			System.out.println("dirty!!!!");
			return;
		}

		Object oldValue = getAttribute(key);
		if ((value != null || oldValue != null)
				&& (value == null && oldValue != null || oldValue == null && value != null
						|| !value.getClass().isInstance(oldValue) || !value.equals(oldValue))) {
			System.out.println("changeAttributes");
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
	 * 从session对象中读入数据,
	 * redis读出的数据由json转为SessionLoginUserInfo,并且把它转换到session的attribute中
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
		maxInactiveInterval = getMaxInactiveInterval()*1000;
		if(userinfo.getUser_auth()!=null){
			Object ei = userinfo.getUser_auth().get("expireIn");
			if(ei!=null){
				if (ei instanceof Number) {
					Number nmn = (Number) ei;
					maxInactiveInterval = nmn.intValue();
				}
				else{
					maxInactiveInterval = Integer.parseInt(ei.toString());
				}
			}
		}
		isNew = userinfo.isNew();
		isValid = userinfo.isValid();
		thisAccessedTime = (Long) System.currentTimeMillis();
		principal = null; // Transient only
		// setId((String) stream.readObject());
		id = (String) userinfo.getId();
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("readObject() loading session " + id);
//		System.out.println("readObject() loading session " + id);
		// Deserialize the attribute count and attribute values
		if (attributes == null)
			attributes = new ConcurrentHashMap<>();
		boolean isValidSave = isValid;
		isValid = true;
		if(log.isTraceEnabled()){
			log.trace("put loginUser attribute  with value '" + userinfo.getUser_auth() + "'");
		}
//		System.out.println("put loginUser attribute  with value '" + userinfo.getUser_auth() + "'");
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger()
					.debug("put loginUser attribute  with value '" + userinfo.getUser_auth() + "'");
		if(userinfo.getUser_auth()!=null){
			attributes.put(redisSessionAttrKey, userinfo.getUser_auth());
		}
		Map<String, Object> attrs = userinfo.getAttrs();
		for (Iterator iterator = attrs.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Object> en = (Map.Entry<String, Object>) iterator.next();
			String name = en.getKey();
			Object value = en.getValue();
			if ((value instanceof String) && (value.equals(NOT_SERIALIZED)))
				continue;
			if(log.isTraceEnabled()){
				log.trace("  loading attribute '" + name + "' with value '" + value + "'");
			}
//			System.out.println("  loading attribute '" + name + "' with value '" + value + "'");
			if (manager.getContext().getLogger().isDebugEnabled())
				manager.getContext().getLogger().debug("  loading attribute '" + name + "' with value '" + value + "'");
			//通过序列化把base64的内容转成对象
			try {
				Object objectFromBase64 = getObjectFromBase64(value);
				if(log.isTraceEnabled()){
					log.trace("  attribute(disSerializer) '" + name + "' with value '" + objectFromBase64 + "'");
				}
//				System.out.println("  attribute(disSerializer) '" + name + "' with value '" + objectFromBase64 + "'");
				if (manager.getContext().getLogger().isDebugEnabled())
					manager.getContext().getLogger().debug("  attribute(disSerializer) '" + name + "' with value '" + objectFromBase64 + "'");
				attributes.put(name,objectFromBase64 );
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	 * 从base64的对象中读取内容
	 * @param value
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private Object getObjectFromBase64(Object value) throws IOException, ClassNotFoundException {
		if(value==null){
			return null;
		}
		if (value instanceof String) {
			String valuestr = (String) value;
			
			byte[] decodeBase64 = Base64.decodeBase64(valuestr);
			Loader loader = null;
		    if (manager.getContext() != null) {
		      loader = manager.getContext().getLoader();
		    }
		    ClassLoader classLoader = null;
		    if (loader != null) {
		        classLoader = loader.getClassLoader();
		    }
		    ObjectInputStream oos = new CustomObjectInputStream(new ByteArrayInputStream(decodeBase64),classLoader);
			try{
				return oos.readObject();
			}
			finally{
				if(oos!=null){
					oos.close();
				}
			}
		}
		else{
			return value;
		}
	}

	/**
	 * 把session信息写成SessionLoginUserInfo 对象,SessionLoginUserInfo会转化为json存入redis
	 * 其中其它的属性,会通过序列化成base64,存到SessionLoginUserInfo的属性中
	 * @return
	 * @throws IOException
	 */
	public SessionLoginUserInfo doWriteObject() throws IOException {

		SessionLoginUserInfo userinfo = new SessionLoginUserInfo();
		userinfo.setCreationTime(creationTime);
		userinfo.setId(id);
		userinfo.setNew(isNew);
		userinfo.setValid(isValid);
		
		Object rawAuthInfo = attributes.get(redisSessionAttrKey);
		if (rawAuthInfo instanceof SessionUserAuthInfo) {
			SessionUserAuthInfo authinfo = (SessionUserAuthInfo) rawAuthInfo;
			if (manager.getContext().getLogger().isDebugEnabled())
				manager.getContext().getLogger().debug(
						"  user login info= '" + authinfo + "'");
			if(log.isTraceEnabled()){
				log.trace("get SessionUserAuthInfo from  session= " +authinfo);
			}
//			System.out.println("get SessionUserAuthInfo from  session= " +authinfo);
			if(authinfo!=null){
				
				userinfo.setUser_auth(authinfo.toMap());
			}
		}
		if (rawAuthInfo instanceof Map) {
			Map map = (Map) rawAuthInfo;
//			System.out.println("get SessionUserAuthInfo(Map) from  session= " +map);
			userinfo.setUser_auth(map);
		}
		else{
			
			System.out.println("in session's loginUserAttr is not the class of SessionUserAuthInfo or map");
			System.out.println(rawAuthInfo!=null?rawAuthInfo.getClass():"is null");
		}
		userinfo.setThisAccessedTime(thisAccessedTime);
		//写入其它的信息
		String keys[] = keys();
		ArrayList<String> saveNames = new ArrayList<>();
		ArrayList<Object> saveValues = new ArrayList<>();
		for (int i = 0; i < keys.length; i++) {
			if(redisSessionAttrKey.equals(keys[i])){
				//此部分信息不需要再保存一次
				continue;
			}
			Object value = attributes.get(keys[i]);
//			System.out.println("attr  key="+keys[i]+",value="+value);
			
			if (value == null)
				continue;
			else if ((value instanceof Serializable) && (!exclude(keys[i]))) {
				saveNames.add(keys[i]);
				String base64 = serial2Base64(value);
				saveValues.add(base64);
				
//			}else if(redisSessionAttrKey.equals(keys[i])){
//				saveNames.add(keys[i]);
//				saveValues.add(value);
			}
			else {
				removeAttributeInternal(keys[i], true);
			}
		}
		int n = saveNames.size();
		for (int i = 0; i < n; i++) {
				userinfo.putAttr(saveNames.get(i), saveValues.get(i));
				if(log.isTraceEnabled()){
					log.trace(
							"  storing attribute '" + saveNames.get(i) + "' with value '" + saveValues.get(i) + "'");
				}
//				System.out.println(
//							"  storing attribute '" + saveNames.get(i) + "' with value '" + saveValues.get(i) + "'");
				if (manager.getContext().getLogger().isDebugEnabled())
					manager.getContext().getLogger().debug(
							"  storing attribute '" + saveNames.get(i) + "' with value '" + saveValues.get(i) + "'");
		}
		if(log.isTraceEnabled()){
			log.trace("writeObject() storing session " + id+",and userinfo="+userinfo);
		}
//		System.out.println("writeObject() storing session " + id+",and userinfo="+userinfo);
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("writeObject() storing session " + id);
		if (manager.getContext().getLogger().isDebugEnabled())
			manager.getContext().getLogger().debug("writeObject() userinfo= " + userinfo);

		return userinfo;

	}

	/**
	 * 把对像序列化为base64字符串
	 * @param value
	 * @return
	 * @throws IOException 
	 */
	private String serial2Base64(Object value) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		try{
			oos.writeObject(value);
			byte[] byteData = os.toByteArray();
			return new String(Base64.encodeBase64((byteData)));
		}
		finally{
			if(oos!=null){
				oos.close();
			}
			if(os!=null){
				os.close();
			}
		}
	}

}
