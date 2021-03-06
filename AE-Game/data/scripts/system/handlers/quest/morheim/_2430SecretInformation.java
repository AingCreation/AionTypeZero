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

package quest.morheim;

import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.questEngine.handlers.QuestHandler;
import org.typezero.gameserver.model.DialogAction;
import org.typezero.gameserver.questEngine.model.QuestEnv;
import org.typezero.gameserver.questEngine.model.QuestState;
import org.typezero.gameserver.questEngine.model.QuestStatus;
import org.typezero.gameserver.services.QuestService;

/**
 * @author MrPoke remod By Nephis
 * @reworked vlog
 */
public class _2430SecretInformation extends QuestHandler {

	private final static int questId = 2430;

	public _2430SecretInformation() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 204327, 204377, 205244, 798081, 798082, 204300 };
		qe.registerQuestNpc(204327).addOnQuestStart(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204327) { // Sveinn
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case ASK_QUEST_ACCEPT: {
						return sendQuestDialog(env, 4);
					}
					case QUEST_REFUSE_1: {
						return sendQuestDialog(env, 1004);
					}
					case QUEST_ACCEPT_1: {
						return sendQuestDialog(env, 1003);
					}
					case FINISH_DIALOG: {
						return sendQuestSelectionDialog(env);
					}
					case SETPRO1: {
						if (player.getInventory().getKinah() >= 500) {
							if (QuestService.startQuest(env)) {
								player.getInventory().decreaseKinah(500);
								changeQuestStep(env, 0, 1, false); // 1
								return sendQuestDialog(env, 1352);
							}
						}
						else {
							return sendQuestDialog(env, 1267);
						}
					}
					case SETPRO3: {
						if (player.getInventory().getKinah() >= 5000) {
							if (QuestService.startQuest(env)) {
								player.getInventory().decreaseKinah(5000);
								changeQuestStep(env, 0, 3, false); // 3
								return sendQuestDialog(env, 2034);
							}
						}
						else {
							return sendQuestDialog(env, 1267);
						}
					}
					case SETPRO7: {
						if (player.getInventory().getKinah() >= 50000) {
							if (QuestService.startQuest(env)) {
								player.getInventory().decreaseKinah(50000);
								changeQuestStep(env, 0, 7, false); // 7
								return sendQuestDialog(env, 3398);
							}
						}
						else {
							return sendQuestDialog(env, 1267);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204327: { // Sveinn
					switch (dialog) {
						case QUEST_SELECT: {
							switch (var) {
								case 1: {
									return sendQuestDialog(env, 1352);
								}
								case 3: {
									return sendQuestDialog(env, 2034);
								}
								case 7: {
									return sendQuestDialog(env, 3398);
								}
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2, 182204221, 1, 0, 0); // 2
						}
						case SETPRO4: {
							return defaultCloseDialog(env, 3, 4); // 4
						}
						case SETPRO8: {
							return defaultCloseDialog(env, 7, 8); // 8
						}
					}
					break;
				}
				case 204377: { // Grall
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SELECT_QUEST_REWARD: {
							removeQuestItem(env, 182204221, 1);
							changeQuestStep(env, 2, 2, true); // reward 0
							return sendQuestDialog(env, 5);
						}
					}
					break;
				}
				case 205244: { // Hugorunerk
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO5: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 798081: { // Nicoyerk
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
						}
						case SETPRO6: {
							return defaultCloseDialog(env, 5, 6); // 6
						}
					}
					break;
				}
				case 798082: { // Bicorunerk
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 6, 6, true); // reward 1
							return sendQuestDialog(env, 6);
						}
					}
					break;
				}
				case 204300: { // Bolverk
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 8) {
								if (player.getInventory().getItemCountByItemId(182204222) > 0) {
									return sendQuestDialog(env, 3739);
								}
								else {
									return sendQuestDialog(env, 3825);
								}
							}
						}
						case SELECT_QUEST_REWARD: {
							removeQuestItem(env, 182204222, 1);
							changeQuestStep(env, 8, 8, true); // reward 2
							return sendQuestDialog(env, 7);
						}
						case FINISH_DIALOG: {
							return sendQuestSelectionDialog(env);
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204377: { // Grall
					if (var == 2) {
						return sendQuestEndDialog(env, 0);
					}
					break;
				}
				case 798082: { // Bicorunerk
					if (var == 6) {
						return sendQuestEndDialog(env, 1);
					}
					break;
				}
				case 204300: { // Bolverk
					if (var == 8) {
						return sendQuestEndDialog(env, 2);
					}
					break;
				}
			}
		}
		return false;
	}
}
