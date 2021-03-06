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

package quest.the_circle;

import org.typezero.gameserver.configs.main.GroupConfig;
import org.typezero.gameserver.model.gameobjects.Npc;
import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.model.team2.group.PlayerGroup;
import static org.typezero.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;
import org.typezero.gameserver.questEngine.handlers.QuestHandler;
import org.typezero.gameserver.model.DialogAction;
import org.typezero.gameserver.questEngine.model.QuestEnv;
import org.typezero.gameserver.questEngine.model.QuestState;
import org.typezero.gameserver.questEngine.model.QuestStatus;
import org.typezero.gameserver.services.QuestService;
import org.typezero.gameserver.utils.MathUtil;
import org.typezero.gameserver.utils.PacketSendUtility;


/**
 * @author Cheatkiller
 *
 */
public class _47110AGlobeTrottingLesson extends QuestHandler {

	private final static int questId = 47110;

	public _47110AGlobeTrottingLesson() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(700971).addOnTalkEvent(questId);
		qe.registerQuestNpc(799921).addOnTalkEvent(questId);
		qe.registerQuestNpc(217174).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 217174, 0, 5);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		}

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 700971) {
				if (player.isInGroup2()){
					PlayerGroup group = player.getPlayerGroup2();
					for (Player member : group.getMembers()) {
						if (player.isMentor() && MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE) {
							Npc npc = (Npc) env.getVisibleObject();
							npc.getController().scheduleRespawn();
							npc.getController().onDelete();
							QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 217174, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
							return true;
						}
						else
							 PacketSendUtility.sendPacket(player, STR_MSG_DailyQuest_Ask_Mentee);
					}
				}
		  }
			if (targetId == 799921) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if(qs.getQuestVarById(0) == 5) {
						return sendQuestDialog(env, 1352);
					}
				}
				else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 5, 5, true, true);
			  }
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799921) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 5);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
