package io.choerodon.devops.infra.template;

import io.choerodon.core.notify.EmailTemplate;
import io.choerodon.devops.infra.constant.NoticeCodeConstants;
import org.springframework.stereotype.Component;

/**
 * User: Mr.Wang
 * Date: 2019/12/20
 */
@Component
public class InviteUserEmailTemplate implements EmailTemplate {
    @Override
    public String code() {
        return "InviteUserEmail";
    }

    @Override
    public String name() {
        return "邀请用户";
    }

    @Override
    public String businessTypeCode() {
        return NoticeCodeConstants.INVITE_USER;
    }

    @Override
    public String title() {
        return "邀请成员";
    }

    @Override
    public String content() {
        return "\n" +
                "<!doctype html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n" +
                "\n" +
                "<head>\n" +
                "  <title> </title>\n" +
                "  <!--[if !mso]><!-- -->\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "  <!--<![endif]-->\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "  <style type=\"text/css\">\n" +
                "    #outlook a {\n" +
                "      padding: 0;\n" +
                "    }\n" +
                "\n" +
                "    .ReadMsgBody {\n" +
                "      width: 100%;\n" +
                "    }\n" +
                "\n" +
                "    .ExternalClass {\n" +
                "      width: 100%;\n" +
                "    }\n" +
                "\n" +
                "    .ExternalClass * {\n" +
                "      line-height: 100%;\n" +
                "    }\n" +
                "\n" +
                "    body {\n" +
                "      margin: 0;\n" +
                "      padding: 0;\n" +
                "      -webkit-text-size-adjust: 100%;\n" +
                "      -ms-text-size-adjust: 100%;\n" +
                "    }\n" +
                "\n" +
                "    table,\n" +
                "    td {\n" +
                "      border-collapse: collapse;\n" +
                "      mso-table-lspace: 0pt;\n" +
                "      mso-table-rspace: 0pt;\n" +
                "    }\n" +
                "\n" +
                "    img {\n" +
                "      border: 0;\n" +
                "      height: auto;\n" +
                "      line-height: 100%;\n" +
                "      outline: none;\n" +
                "      text-decoration: none;\n" +
                "      -ms-interpolation-mode: bicubic;\n" +
                "    }\n" +
                "\n" +
                "    p {\n" +
                "      display: block;\n" +
                "      margin: 13px 0;\n" +
                "    }\n" +
                "  </style>\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <style type=\"text/css\">\n" +
                "    @media only screen and (max-width:480px) {\n" +
                "      @-ms-viewport {\n" +
                "        width: 320px;\n" +
                "      }\n" +
                "      @viewport {\n" +
                "        width: 320px;\n" +
                "      }\n" +
                "    }\n" +
                "  </style>\n" +
                "  <!--<![endif]-->\n" +
                "  <!--[if mso]>\n" +
                "  <xml>\n" +
                "    <o:OfficeDocumentSettings>\n" +
                "      <o:AllowPNG/>\n" +
                "      <o:PixelsPerInch>96</o:PixelsPerInch>\n" +
                "    </o:OfficeDocumentSettings>\n" +
                "  </xml>\n" +
                "  <![endif]-->\n" +
                "  <!--[if lte mso 11]>\n" +
                "  <style type=\"text/css\">\n" +
                "    .outlook-group-fix { width:100% !important; }\n" +
                "  </style>\n" +
                "  <![endif]-->\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <link href=\"https://fonts.googleapis.com/css?family=Ubuntu:300,400,500,700\" rel=\"stylesheet\" type=\"text/css\">\n" +
                "  <style type=\"text/css\">\n" +
                "    @import url(https://fonts.googleapis.com/css?family=Ubuntu:300,400,500,700);\n" +
                "  </style>\n" +
                "  <!--<![endif]-->\n" +
                "  <style type=\"text/css\">\n" +
                "    @media only screen and (min-width:480px) {\n" +
                "      .mj-column-per-100 {\n" +
                "        width: 100% !important;\n" +
                "        max-width: 100%;\n" +
                "      }\n" +
                "    }\n" +
                "  </style>\n" +
                "  <style type=\"text/css\">\n" +
                "  </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "<div style=\"\">\n" +
                "  <!--[if mso | IE]>\n" +
                "  <table\n" +
                "          align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"\" style=\"width:600px;\" width=\"600\"\n" +
                "  >\n" +
                "    <tr>\n" +
                "      <td style=\"line-height:0px;font-size:0px;mso-line-height-rule:exactly;\">\n" +
                "  <![endif]-->\n" +
                "  <div style=\"Margin:0px auto;max-width:600px;\">\n" +
                "    <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"width:100%;\">\n" +
                "      <tbody>\n" +
                "      <tr>\n" +
                "        <td style=\"direction:ltr;font-size:0px;padding:20px 0;text-align:center;vertical-align:top;\">\n" +
                "          <!--[if mso | IE]>\n" +
                "          <table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "\n" +
                "            <tr>\n" +
                "\n" +
                "              <td\n" +
                "                      class=\"\" style=\"vertical-align:top;width:600px;\"\n" +
                "              >\n" +
                "          <![endif]-->\n" +
                "          <div class=\"mj-column-per-100 outlook-group-fix\" style=\"font-size:13px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;\">\n" +
                "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"vertical-align:top;\" width=\"100%\">\n" +
                "              <tr>\n" +
                "                <td align=\"left\" style=\"font-size:0px;padding:10px 25px;word-break:break-word;\">\n" +
                "                  <div style=\"font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:13px;line-height:1;text-align:left;color:#000000;\">\n" +
                "\n" +
                "                    <body>\n" +
                "                    <div style=\"width: 600px;background: #FFFFFF;margin: 0 auto;\">\n" +
                "                      <div style=\"height: 115px;background-color: #ffffff;border-bottom: 8px solid #3F51B5;\">\n" +
                "                        <table>\n" +
                "                          <tr>\n" +
                "                            <td style=\"padding-bottom: 0;vertical-align: bottom;\">\n" +
                "                              <table>\n" +
                "                                <tr>\n" +
                "                                  <td style=\"width:190px;\"> <img height=\"auto\" src=\"https://file.choerodon.com.cn/static/choerodon.png\" style=\"border:0;display:block;outline:none;text-decoration:none;height:auto;width: 190px;\" width=\"100%\"  /> </td>\n" +
                "                                </tr>\n" +
                "                              </table>\n" +
                "                            <td>\n" +
                "                            <td style=\"vertical-align: bottom;\">\n" +
                "                              <div style=\"padding-top: 1px;margin-left: 10px; height: 90px; margin-top: 10px; \">\n" +
                "                                <p style=\"font-size: 14px;color: #252528;letter-spacing: 0;display: inline-block;text-align: right;width: 558px;padding-top: 55px;\"></p>\n" +
                "                              </div>\n" +
                "                            </td>\n" +
                "                          </tr>\n" +
                "                        </table>\n" +
                "                      </div>\n" +
                "                      <div style=\"padding: 68px 40px;border-bottom: 0;border-top: 0;border-left:1px solid #E6E6E6;border-right:1px solid #E6E6E6;border-radius: 2px;background: #FFFFFF;\">\n" +
                "                        <div>\n" +
                "                          <p style=\"margin-top: 0\">亲爱的${email}，您好！</p>\n" +
                "                          <p style=\"text-align: justify; margin-bottom: 0; line-height: 14px; font-size: 14px\">${inviteBy}邀请您加入项目：&nbsp;${project} &nbsp;。请点击以下按钮接受邀请并加入。</p>\n" +
                "                          \n" +
                "                        </div>\n" +
                "                        <div class=\"mj-column-per-100 outlook-group-fix\" style=\"font-size:13px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;\">\n" +
                "                          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"vertical-align:top;\" width=\"100%\">\n" +
                "                            <tr>\n" +
                "                              <td align=\"center\" vertical-align=\"middle\" style=\"font-size:0px;padding:10px 25px;padding-top:60px;word-break:break-word;\">\n" +
                "                                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" role=\"presentation\" style=\"border-collapse:separate;width:212px;line-height:100%;\">\n" +
                "                                  <tr>\n" +
                "                                    <td align=\"center\" bgcolor=\"#3f51b5\" role=\"presentation\" style=\"border:none;border-radius:3px;cursor:auto;height:20px;padding:10px 25px;background:#3f51b5;\" valign=\"middle\"> <a href=\"${link}\" style=\"background:#3f51b5;color:#ffffff;font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;\" target=\"_blank\">\n" +
                "                                      完善信息并加入\n" +
                "                                    </a> </td>\n" +
                "                                  </tr>\n" +
                "                                </table>\n" +
                "                              </td>\n" +
                "                            </tr>\n" +
                "                          </table>\n" +
                "                        </div>\n" +
                "                        <p style=\"text-align: justify; margin-bottom: 0; line-height: 14px; font-size: 14px\">如果按钮无法点击，请点击或复制以下链接到浏览器地址栏打开。</p>\n" +
                "                        <p style=\"text-align: justify; margin-bottom: 0; line-height: 14px; font-size: 14px\">${link}</p>\n" +
                "\n" +
                "                      </div>\n" +
                "                      <div>\n" +
                "                        <div style=\"text-align: justify;\">\n" +
                "                          <table style=\"height: 148px; background-color: #f7f7f7;width: 100%;\">\n" +
                "                            <tr>\n" +
                "                              <td style=\"padding-left: 29px;\">\n" +
                "                                <table>\n" +
                "                                  <tr>\n" +
                "                                    <td> <img height=\"auto\" style=\"border:0;display:block;outline:none;text-decoration:none;height:auto;width:90px;\" width=\"90\" src=\"https://file.choerodon.com.cn/static/wechat-code.jpg\"> </tr>\n" +
                "                                  <tr>\n" +
                "                                    <td style=\"text-align: center\">微信公众号</td>\n" +
                "                                  </tr>\n" +
                "                                </table>\n" +
                "                              <td>\n" +
                "                              <td>\n" +
                "                                <div style=\"padding-top: 1px;margin-left: 10px; height: 90px; margin-top: 10px; vertical-align: top;\">\n" +
                "                                  <p style=\"font-size: 12px;color: #626774;letter-spacing: 0;text-align: left;margin-bottom: 6px;     margin-top: 0;\"> 此邮件为系统邮件，请勿回复。 </p>\n" +
                "                                  <p style=\"font-size: 12px;color: #626774;letter-spacing: 0;text-align: left;margin-bottom: 6px;      margin-top: 0;\"> 如果您还尚未掌握Choerodon猪齿鱼的功能和操作，可以访问 <a style=\"text-decoration:none;font-size: 12px\" href=\"http://choerodon.io/zh/\" target=\"_blank\">猪齿鱼官网</a>。 </p>\n" +
                "                                  <p style=\"font-size: 12px;color: #626774;letter-spacing: 0;text-align: left;margin-bottom: 6px;      margin-top: 0;\"> 如果您需要任何帮助或者提供反馈, 请访问 <a style=\"text-decoration:none;font-size: 12px\" href=\"http://forum.choerodon.io/\" target=\"_blank\">Choerodon论坛</a>。 </p>\n" +
                "                                  <p style=\"font-size: 12px;color: #626774; letter-spacing: 0;text-align: left;margin-bottom: 6px;      margin-top: 0;\">您也可以通过发送邮件的方式与我们联系：service@choerodon.com</p>\n" +
                "                                </div>\n" +
                "                              </td>\n" +
                "                            </tr>\n" +
                "                          </table>\n" +
                "                        </div>\n" +
                "                      </div>\n" +
                "                      <div style=\"width: 600px;height: 34px;background: #3F51B5;\">\n" +
                "                        <p style=\"opacity: 0.8;font-size: 10px;color: #FFFFFF;text-align: center;display: inline-block;line-height: 34px;width: 100%;margin: 0 auto;\">Copyright © The Choerodon Author. All rights reserved.</p>\n" +
                "                      </div>\n" +
                "                    </div>\n" +
                "                    </body>\n" +
                "                  </div>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "            </table>\n" +
                "          </div>\n" +
                "          <!--[if mso | IE]>\n" +
                "          </td>\n" +
                "\n" +
                "          </tr>\n" +
                "\n" +
                "          </table>\n" +
                "          <![endif]-->\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "  </div>\n" +
                "  <!--[if mso | IE]>\n" +
                "  </td>\n" +
                "  </tr>\n" +
                "  </table>\n" +
                "  <![endif]-->\n" +
                "</div>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }
}
