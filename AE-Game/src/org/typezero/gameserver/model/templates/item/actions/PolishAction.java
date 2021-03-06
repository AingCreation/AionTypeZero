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

package org.typezero.gameserver.model.templates.item.actions;

import com.aionemu.commons.database.dao.DAOManager;
import org.typezero.gameserver.controllers.observer.ItemUseObserver;
import org.typezero.gameserver.dao.ItemStoneListDAO;
import org.typezero.gameserver.dataholders.DataManager;
import org.typezero.gameserver.model.DescriptionId;
import org.typezero.gameserver.model.TaskId;
import javax.xml.bind.annotation.*;

import org.typezero.gameserver.model.gameobjects.Item;
import org.typezero.gameserver.model.gameobjects.PersistentState;
import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.model.items.IdianStone;
import org.typezero.gameserver.model.items.RandomBonusResult;
import org.typezero.gameserver.model.templates.item.bonuses.StatBonusType;
import org.typezero.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import org.typezero.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import org.typezero.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import org.typezero.gameserver.utils.PacketSendUtility;
import org.typezero.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolishAction")
public class PolishAction extends AbstractItemAction {

	@XmlAttribute(name = "set_id")
	protected int polishSetId;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		if (parentItem.getItemTemplate().getLevel() > targetItem.getItemTemplate().getLevel()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401649));
			return false;
		}
		// to do You need to tune your equipment before socketing Idian.
		/*if (targetItem.hasTune()) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401750));
			return false;
		}*/
		return !player.isAttackMode() && targetItem.getItemTemplate().isWeapon() && targetItem.getItemTemplate().isCanPolish();
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem) {
		final int parentItemId = parentItem.getItemId();
		final int parntObjectId = parentItem.getObjectId();
		final int parentNameId = parentItem.getNameId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(),
				parentItem.getObjectId(), parentItemId, 5000, 0, 0), true);
		final ItemUseObserver observer = new ItemUseObserver() {
			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(new DescriptionId(parentNameId)));
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(),
						parntObjectId, parentItemId, 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};
		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);

				PacketSendUtility.broadcastPacket(player,
						new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parntObjectId, parentItemId, 0, 1, 1), true);
				if (!player.getInventory().decreaseByObjectId(parntObjectId, 1)) {
					return;
				}
				RandomBonusResult bonus = DataManager.ITEM_RANDOM_BONUSES.getRandomModifiers(StatBonusType.POLISH, polishSetId);
				if (bonus == null) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_FAILED(new DescriptionId(parentNameId)));
					return;
				}
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401650, "[item_ex:"+ targetItem.getItemId()+";"+targetItem.getItemName()+"]"));
				IdianStone idianStone = targetItem.getIdianStone();
				if (idianStone!= null) {
					idianStone.onUnEquip(player);
					targetItem.setIdianStone(null);
					idianStone.setPersistentState(PersistentState.DELETED);
					DAOManager.getDAO(ItemStoneListDAO.class).storeIdianStones(idianStone);
				}
				idianStone = new IdianStone(parentItemId, PersistentState.NEW, targetItem, bonus.getTemplateNumber(), 1000000);
				targetItem.setIdianStone(idianStone);
				if (targetItem.isEquipped()) {
					idianStone.onEquip(player);
				}
				PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, targetItem));
			}

		}, 5000));

	}

	public int getPolishSetId() {
		return polishSetId;
	}

}
