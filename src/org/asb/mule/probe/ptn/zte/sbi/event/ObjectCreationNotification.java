package org.asb.mule.probe.ptn.zte.sbi.event;

import org.omg.CosNotification.*;

import java.util.Arrays;
import java.util.Hashtable;


/**
  *	Class contains information used in object creation notification.
*/  
public  class ObjectCreationNotification implements java.io.Serializable
{
	
	public ObjectCreationNotification(StructuredEvent notification)
	{
		for(int i =0 ;i < notification.filterable_data.length; i++){
			Property property = notification.filterable_data[i];

			properties.put(property.name, property.value);
		}

		createdObject = notification.remainder_of_body;
	}


	public String notificationId()
	throws Exception
	{
		org.omg.CORBA.Any value = lookupProperty("notificationId");

		return value.extract_string();
	}

	
	public globaldefs.NameAndStringValue_T[] objectName()
	throws Exception
	{
		org.omg.CORBA.Any value = lookupProperty("objectName");

		return globaldefs.NamingAttributes_THelper.extract(value);
	}

	public notifications.ObjectType_T objectType()
	throws Exception
	{
		org.omg.CORBA.Any value = lookupProperty("objectType");

		return  notifications.ObjectType_THelper.extract(value);
	}

	public String emsTime()
	throws Exception
	{
		org.omg.CORBA.Any value = lookupProperty("emsTime");

		return value.extract_string();
	}

	public String neTime()
	throws Exception
	{
		org.omg.CORBA.Any value = lookupProperty("neTime");

		return value.extract_string();
	}

	public boolean edgePointRelated()
	throws Exception
	{
		org.omg.CORBA.Any value = lookupProperty("edgePointRelated");

		return value.extract_boolean();
	}


	public org.omg.CORBA.Any createdObject()
	{
		return createdObject;

	}
	private org.omg.CORBA.Any lookupProperty(String name)
	throws Exception
	{
		if(null == properties.get(name))
			throw new Exception("Can't find property: " + name);

		return (org.omg.CORBA.Any)properties.get(name);
	}

	//
	// Attributes.
	//

	// Key: String, name of property, value: org.omg.corba.any, value of property.
	private Hashtable properties = new Hashtable();
		
	private org.omg.CORBA.Any createdObject;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb =new StringBuffer();
		try {
			sb.append("ObjectCreationNotification [edgePointRelated()="
					+ edgePointRelated() + ", emsTime()=" + emsTime()
					+ ", neTime()=" + neTime() + ", notificationId()="
					+ notificationId() + ", objectName()="
					+ Arrays.toString(objectName()) + ", objectType()="
					+ objectType() + "]");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}finally{
			return sb.toString();
		}
	}
		
}

