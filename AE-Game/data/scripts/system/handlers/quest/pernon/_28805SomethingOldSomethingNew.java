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

package quest.pernon;

import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.questEngine.handlers.QuestHandler;
import org.typezero.gameserver.model.DialogAction;
import org.typezero.gameserver.questEngine.model.QuestEnv;
import org.typezero.gameserver.questEngine.model.QuestState;
import org.typezero.gameserver.questEngine.model.QuestStatus;

/**
 *
 * @author Ritsu
 */
public class _28805SomethingOldSomethingNew extends QuestHandler
{

	private static final int questId = 28805;

	public _28805SomethingOldSomethingNew()
	{
		super(questId);
	}

	@Override
	public void register()
	{
		qe.registerQuestNpc(830154).addOnQuestStart(questId);
		qe.registerQuestNpc(830154).addOnTalkEvent(questId);
		qe.registerQuestNpc(830521).addOnTalkEvent(questId);
		qe.registerQuestNpc(830662).addOnTalkEvent(questId);
		qe.registerQuestNpc(830663).addOnTalkEvent(questId);
		qe.registerQuestNpc(730525).addOnTalkEvent(questId);
		qe.registerQuestNpc(730522).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env)
	{
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if(qs == null || qs.getStatus() == QuestStatus.NONE)
		{
			if (targetId == 830154)
			{
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}
		else if (qs.getStatus() == QuestStatus.START)
		{
			int var = qs.getQuestVarById(0);
			switch (targetId)
			{
				case 830521:
				case 830662:
				case 830663:
				{
					switch (dialog)
					{
						case QUEST_SELECT:
						{
							if (var == 0)
								return sendQuestDialog(env, 1352);
							else if (var == 2)
								return sendQuestDialog(env, 2375);
						}
						case SETPRO1:
						{
							return defaultCloseDialog(env, 0, 1);
						}
						case SELECT_QUEST_REWARD:
						{
							changeQuestStep(env, 2, 2, true);
							return sendQuestDialog(env, 5);
						}
					}
				}
				case 730525:
				case 730522:
				{
					switch (dialog)
					{
						case USE_OBJECT:
						{
							if (var == 1)
								return sendQuestDialog(env, 1693);
						}
						case SETPRO2:
						{
							return defaultCloseDialog(env, 1, 2);
						}
					}
				}

			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
		{
			switch (targetId)
			{
				case 830521:
				case 830662:
				case 830663:
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
