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

package org.typezero.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.typezero.gameserver.model.gameobjects.Creature;
import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.model.gameobjects.player.RequestResponseHandler;
import org.typezero.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import org.typezero.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import org.typezero.gameserver.services.teleport.TeleportService2;
import org.typezero.gameserver.skillengine.model.Effect;
import org.typezero.gameserver.utils.PacketSendUtility;

/**
 * @author Bio, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecallInstantEffect")
public class RecallInstantEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		final Creature effector = effect.getEffector();
		final Player effected = (Player) effect.getEffected();

		final int worldId = effect.getWorldId();
		final int instanceId = effect.getInstanceId();
		final float locationX = effect.getSkill().getX();
		final float locationY = effect.getSkill().getY();
		final float locationZ = effect.getSkill().getZ();
		final byte locationH = effect.getSkill().getH();

		/**
		 * TODO need to confirm if cannot be summoned while on abnormal effects stunned, sleeping, feared, etc.
		 */
		RequestResponseHandler rrh = new RequestResponseHandler(effector) {

			@Override
			public void denyRequest(Creature effector, Player effected) {
				PacketSendUtility.sendPacket((Player) effector,
					SM_SYSTEM_MESSAGE.STR_MSG_Recall_Rejected_EFFECT(effected.getName()));
				PacketSendUtility.sendPacket(effected, SM_SYSTEM_MESSAGE.STR_MSG_Recall_Rejected_EFFECT(effector.getName()));
			}

			@Override
			public void acceptRequest(Creature effector, Player effected) {
				TeleportService2.teleportTo(effected, worldId, instanceId, locationX, locationY, locationZ, locationH);
			}
		};

		effected.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_SUMMON_PARTY_DO_YOU_ACCEPT_REQUEST, rrh);
		PacketSendUtility.sendPacket(effected, new SM_QUESTION_WINDOW(
			SM_QUESTION_WINDOW.STR_SUMMON_PARTY_DO_YOU_ACCEPT_REQUEST, 0, 0, effector.getName(), "Summon Group Member", 30));
	}

	@Override
	public void calculate(Effect effect) {
		final Creature effector = effect.getEffector();

		if (!(effect.getEffected() instanceof Player))
			return;
		Player effected = (Player)effect.getEffected();

		if (effected.getController().isInCombat())
			return;

		if (effector.getWorldId() == effected.getWorldId() && !effector.isInInstance()
			&& !(effector.isEnemy(effected))) {
			effect.getSkill().setTargetPosition(effector.getX(), effector.getY(), effector.getZ(), effector.getHeading());
			effect.addSucessEffect(this);
		}
	}
}
