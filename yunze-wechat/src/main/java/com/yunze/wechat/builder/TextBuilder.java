package com.yunze.wechat.builder;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTransferKefuMessage;


/**
 * @author
 */
public class TextBuilder extends AbstractBuilder {


	@Override
	public WxMpXmlOutMessage build(String content, WxMpXmlMessage wxMessage, WxMpService service) {
		WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content(content).fromUser(wxMessage.getToUser())
				.toUser(wxMessage.getFromUser()).build();
		return m;
	}

	/**
	 * @Return: 客服接入
	 */
	public WxMpXmlOutTransferKefuMessage buildKf(String content, WxMpXmlMessage wxMessage, WxMpService service) {
		WxMpXmlOutTransferKefuMessage m = WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
				.toUser(wxMessage.getFromUser()).build();
		return m;
	}

}
