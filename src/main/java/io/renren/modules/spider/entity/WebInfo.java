package io.renren.modules.spider.entity;

import java.io.Serializable;

/**
 * @Author wgsh
 * @Date wgshb on 2018/11/19 10:53
 */
public class WebInfo implements Serializable {

	private String ID;
	private String Title;

	private String WebName;
	private String IssueTime;

	private String WebInfoContent;
	private String attachfiles;



	public String getID() {
		return ID;
	}

	public String getAttachfiles() {
		return attachfiles;
	}

	public void setAttachfiles(String attachfiles) {
		this.attachfiles = attachfiles;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getWebName() {
		return WebName;
	}

	public void setWebName(String webName) {
		WebName = webName;
	}

	public String getIssueTime() {
		return IssueTime;
	}

	public void setIssueTime(String issueTime) {
		IssueTime = issueTime;
	}

	public String getWebInfoContent() {
		return WebInfoContent;
	}

	public void setWebInfoContent(String webInfoContent) {
		WebInfoContent = webInfoContent;
	}

	@Override
	public String toString() {
		return "WebInfo [ID=" + ID + ", Title=" + Title + ", WebName=" + WebName + ", IssueTime=" + IssueTime
				+ ", WebInfoContent=" + WebInfoContent + "]";
	}

}
