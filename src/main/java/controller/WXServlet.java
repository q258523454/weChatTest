package controller;

import entity.AccessToken;
import entity.TextMessage;
import org.apache.log4j.Logger;
import util.CheckUtil;
import util.MessageUtil;
import util.WeChatUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class WXServlet extends HttpServlet {

    private Logger logger = Logger.getLogger(WXServlet.class);

    // 接请求URL而不需要传递参数时，可使用Get请求, 需要传递参数的时候，就需要使用Post请求
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String signature = request.getParameter("signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        PrintWriter out = response.getWriter();
        if (CheckUtil.checkSignature(signature, timestamp, nonce)) {
            //如果校验成功，将得到的随机字符串原路返回
            out.print(echostr);
        }

    }

    // 超过5秒, 微信服务器不会处理
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 从微信服务器接受post, 微信服务器--->本地(用doPost接受)
        long startTime = System.currentTimeMillis();    //获取开始时间
        response.setCharacterEncoding("UTF-8");         // 避免中文乱码
        PrintWriter out = response.getWriter();
        String str = "";
        // 睡眠5秒
//        try {
//            Thread.currentThread().sleep(5000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            Map<String, String> map = MessageUtil.xmlToMap(request);
            // 取元素
            String ToUserName = map.get("ToUserName");          // 开发者微信号
            String FromUserName = map.get("FromUserName");      // 发送方(接收方)
            String CreateTime = map.get("CreateTime");
            String MsgType = map.get("MsgType");
            String Content = map.get("Content");
            String MsgId = map.get("MsgId");
            String MediaId = map.get("MediaId");

            if (MsgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                TextMessage message = new TextMessage();
                message.setFromUserName(ToUserName);
                message.setToUserName(FromUserName);
                message.setMsgType("text");
                message.setCreateTime(simpleDateFormat.format(new Date()));
                message.setMsgId(MsgId);
                message.setContent("Hello World!");
                str = MessageUtil.textMsgToXml(message);
            } else if (MsgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {//判断是否为文本消息类型
                if (Content.equals("1")) {
                    str = MessageUtil.initText(ToUserName, FromUserName, MsgId,"A");
                } else if (Content.equals("2")) {
                    str = MessageUtil.initText(ToUserName, FromUserName, MsgId,"B");
                } else if (Content.equals("3")) {
                    str = MessageUtil.initText(ToUserName, FromUserName, MsgId,MessageUtil.menuText());
                } else if(Content.equals("4")){
                    str = MessageUtil.initNewsMessage(ToUserName, FromUserName,MsgId);
                } else if (Content.equals("5")) {
                    AccessToken accessToken = WeChatUtil.getAccessToken();
                    str = MessageUtil.initText(ToUserName, FromUserName, MsgId, "AccessToken:" + accessToken.getToken() + "\ntime:" + accessToken.getExpiresIn());
                } else if(Content.equals("6")){
                    // 自动回复临时素材, 新增素材(临时,永久)在 UpLoadImage.main() 执行
                    str = MessageUtil.initImageMessage(ToUserName, FromUserName, "_vgZdlsWMmR3wTEIqyCFC8aNsJCvZ3np0fn_l3zuI3o");
                }else if(Content.equals("7")){
                    // 自动回复音乐
                    str = MessageUtil.initMusicMessage(ToUserName, FromUserName, "fK1DyhiZslykAqhNFms8GIBhnAOPa5wy6sr67DnAk9ONUfaPCrqgJWTtMpYJ0v4k");
                }else {
                    // 菜单的创建在 MenuTest 类执行 Main()
                    str = MessageUtil.initText(ToUserName, FromUserName, MsgId, "选项超出范围！");
                }
            } else if (MsgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {    //判断是否为事件类型
                String eventType = map.get("Event");
                if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
                    str = MessageUtil.initText(ToUserName, FromUserName, MsgId, MessageUtil.menuText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
        logger.info(("程序运行时间：" + (endTime - startTime) + "ms"));    //输出程序运行时间
        out.print(str);
        out.close();
    }


}
