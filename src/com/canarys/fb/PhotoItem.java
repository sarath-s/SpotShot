/*
FacebookAlbumDemo - Example of how to display Facebook albums in an Android application
Copyright (C) 2010-2011 Hugues Johnson

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package com.canarys.fb;

public class PhotoItem{
	private String pictureUrl;
	private String sourceUrl;
	
	public PhotoItem(String pictureUrl,String sourceUrl){
		this.pictureUrl=pictureUrl;
		this.sourceUrl=sourceUrl;
	}
	
	public String getPictureUrl(){
		return pictureUrl;
	}
	public void setPictureUrl(String pictureUrl){
		//fix for Android 2.3
		CharSequence target="\\/";
		CharSequence replace="/";
		String fixedUrl=pictureUrl.replace(target,replace);
		this.pictureUrl=fixedUrl;
	}
	
	public String getSourceUrl(){
		return sourceUrl;
	}
	
	public void setSourceUrl(String sourceUrl){
		this.sourceUrl=sourceUrl;
	}
}