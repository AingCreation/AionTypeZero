/*
 * Copyright (c) 2015, TypeZero Engine (game.developpers.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of TypeZero Engine nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.typezero.gameserver.dataholders;

import org.typezero.gameserver.model.templates.teleport.ScrollItem;
import org.typezero.gameserver.model.templates.teleport.ScrollItemLocationList;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author GiGatR00n
 */
@XmlRootElement(name = "item_multi_returns")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiReturnItemData {

	@XmlElement(name = "item")
	private List<ScrollItem> ItemList;

	private TIntObjectHashMap<List<ScrollItemLocationList>> ItemLocationList = new TIntObjectHashMap<List<ScrollItemLocationList>>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		ItemLocationList.clear();
		for (ScrollItem template : ItemList)
			ItemLocationList.put(template.getId(), template.getLocationList());
	}

	public int size() {
		return ItemLocationList.size();
	}

	public ScrollItem getScrollItembyId(int id) {

		for (ScrollItem template : ItemList) {
			if (template.getId() == id) {
				return template;
			}
		}
		return null;
	}

	public List<ScrollItem> getScrollItems() {
		return ItemList;
	}
}
