package com.yunze.common.utils.yunze;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shenpei
 *
 */
public class XmlUtil {

	static Map<String, Object> xmlMap = new HashMap<String, Object>();

	/**
	 * xml字符串转换成Map 获取标签内属性值和text值
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> xmlToMap(String xml) throws Exception {
		StringReader reader = new StringReader(xml);
		InputSource source = new InputSource(reader);
		SAXReader sax = new SAXReader(); // 创建一个SAXReader对象
		Document document = sax.read(source); // 获取document对象,如果文档无节点，则会抛出Exception提前结束
		Element root = document.getRootElement(); // 获取根节点
		Map<String, Object> map = XmlUtil.getNodes(root); // 从根节点开始遍历所有节点
		return map;
	}

	/**
	 * 从指定节点开始,递归遍历所有子节点
	 *
	 * @author chenleixing
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getNodes(Element node) {
		xmlMap.put(node.getName().toLowerCase(), node.getTextTrim());
		List<Attribute> listAttr = node.attributes(); // 当前节点的所有属性的list
		for (Attribute attr : listAttr) { // 遍历当前节点的所有属性
			String name = attr.getName(); // 属性名称
			String value = attr.getValue(); // 属性的值
			xmlMap.put(name, value.trim());
		}

// 递归遍历当前节点所有的子节点
		List<Element> listElement = node.elements(); // 所有一级子节点的list
		for (Element e : listElement) { // 遍历所有一级子节点
			XmlUtil.getNodes(e); // 递归
		}
		return xmlMap;

	}

}
