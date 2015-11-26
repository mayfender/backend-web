package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Image implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String imageName;
	private byte imageContent[];
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="image_type_id", referencedColumnName="id")
	private ImageType imageType;
	
	protected Image() {}
	
	public Image(String imageName, byte imageContent[], ImageType imageType) {
		this.imageName = imageName;
		this.imageContent = imageContent;
		this.imageType = imageType;
	}
	
	@Override
	public String toString() {
		
		ReflectionToStringBuilder stringBuilder = new ReflectionToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		stringBuilder.setAppendStatics(true);
		stringBuilder.setAppendTransients(true);
		stringBuilder.setExcludeFieldNames("imageContent");
		
		return stringBuilder.toString();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public byte[] getImageContent() {
		return imageContent;
	}
	public void setImageContent(byte[] imageContent) {
		this.imageContent = imageContent;
	}
	public ImageType getImageType() {
		return imageType;
	}
	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

}
