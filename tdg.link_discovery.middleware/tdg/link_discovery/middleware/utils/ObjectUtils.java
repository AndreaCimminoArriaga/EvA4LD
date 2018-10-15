package tdg.link_discovery.middleware.utils;

import java.lang.reflect.Constructor;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.ILearner;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;



public class ObjectUtils {

	
	public static Object createObject(String classFullpathName, Class<?>[] argumentTypes, Object[] arguments){
		Object object = null;
		try{
			
			Class<?> myClass = Class.forName(classFullpathName);
			Constructor<?> constructor = myClass.getConstructor(argumentTypes);
			object = constructor.newInstance(arguments);
		}catch(Exception e){
			System.out.println("Exception in ObjectUtils::createObject");
			
			e.printStackTrace();
		}
		return object;
	}
	
	public static Object createObjectByName(String className, Class<?>[] argumentTypes, Object[] arguments){
		Object object = null;
		try{
			Class<?> myClass = Class.forName(className);
			Constructor<?> constructor = myClass.getConstructor(argumentTypes);
			object = constructor.newInstance(arguments);
		}catch(Exception e){
			System.out.println("Exception in ObjectUtils::createObject");
			e.printStackTrace();
		}
		return object;
	}
	
	
	
	public static ILearner createAlgorithm(String className, IEnvironment environment, Setup setup){
		ILearner learner = null;
		try{
			Class<?> myClass = Class.forName(className);
			Class<?>[] types = {IEnvironment.class, Setup.class};
			Constructor<?> constructor = myClass.getConstructor(types);
			Object[] parameters = {environment, setup};
			learner  = (ILearner) constructor.newInstance(parameters);
			System.out.println("¿?");
		}catch(Exception e){
			System.out.println("Exception in ObjectUtils::createObject");
			e.printStackTrace();
		}
		return learner;
	}
}
