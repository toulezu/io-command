package com.ckjava.io.command;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassInstanceHelper {

	private static Logger logger = LoggerFactory.getLogger(ClassInstanceHelper.class);

	/**
	 * 
	 * @param className
	 *            类路劲的名字
	 * @return 返回根据className指明的类信息
	 */
	public static Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			logger.error("ClassInstanceHelper getClass has error, Exception:{}", e.getClass().getName());
			return null;
		}
	}

	/**
	 * 
	 * @param name
	 *            类路径
	 * @param classParas
	 *            Class类信息参数列表 如果是基本数据类型是可以使用其Tpye类型，如果用class字段是无效的
	 *            如果是非数据类型可以使用的class字段来创建其Class类信息对象，这些都要遵守。
	 * @param paras
	 *            实际参数列表数据
	 * @return 返回Object引用的对象，实际实际创建出来的对象，如果要使用可以强制转换为自己想要的对象
	 * 
	 *         带参数的反射创建对象
	 */
	public static Object getInstance(String name, Class<?> classParas[], Object paras[]) {
		try {
			Class<?> c = getClass(name);
			Constructor<?> con = c.getConstructor(classParas);// 获取使用当前构造方法来创建对象的Constructor对象，用它来获取构造函数的一些
				
			return con.newInstance(paras);// 传入当前构造函数要的参数列表
		} catch (Exception e) {
			logger.error("ClassInstanceHelper getInstance has error, Exception:{}", e.getClass().getName());
			return null;
		}

	}
	
	/**
	 * 
	 * @param name
	 *            类路径
	 * @param paras
	 *            实际参数列表数据, 要确保这些参数不是基础 
	 * @return 返回Object引用的对象，实际实际创建出来的对象，如果要使用可以强制转换为自己想要的对象
	 * 
	 *         带参数的反射创建对象
	 */
	public static Object getInstance(String name, Object paras[]) {
		try {
			Class<?> clazz = getClass(name);
			
			Class<?>[] classParas = new Class<?>[paras.length];
			for (int i = 0, c = paras.length; i < c; i++) {
				classParas[i] = paras[i].getClass();
			}
			Constructor<?> con = clazz.getConstructor(classParas);// 获取使用当前构造方法来创建对象的Constructor对象，用它来获取构造函数的一些
				
			return con.newInstance(paras);// 传入当前构造函数要的参数列表
		} catch (Exception e) {
			logger.error("ClassInstanceHelper getInstance has error, Exception:{}", e.getClass().getName());
			return null;
		}

	}

	/**
	 * 
	 * @param name
	 *            类路径
	 * @return 不带参数的反射创建对象
	 */
	public static Object getInstance(String name) {
		try {
			Class<?> c = getClass(name);
			return c.newInstance();
		} catch (Exception e) {
			logger.error("ClassInstanceHelper getInstance has error, Exception:{}", e.getClass().getName());
			return null;
		}
	}
	
}
