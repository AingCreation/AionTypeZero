package quest.morheim;

import org.typezero.gameserver.model.DialogAction;
import org.typezero.gameserver.model.gameobjects.Item;
import org.typezero.gameserver.model.gameobjects.Npc;
import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.questEngine.handlers.HandlerResult;
import org.typezero.gameserver.questEngine.handlers.QuestHandler;
import org.typezero.gameserver.questEngine.model.QuestEnv;
import org.typezero.gameserver.questEngine.model.QuestState;
import org.typezero.gameserver.questEngine.model.QuestStatus;
import org.typezero.gameserver.services.QuestService;
import org.typezero.gameserver.world.zone.ZoneName;

/**
 * @author Romanz
 */
public class _24022SneakBehindtheIceClaw extends QuestHandler {

	private final static int questId = 24022;

	public _24022SneakBehindtheIceClaw() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 204303, 204332, 700246, 204301, 204399, 802047 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(204417).addOnKillEvent(questId);
		qe.registerQuestNpc(212877).addOnKillEvent(questId);
		qe.registerQuestItem(182215364, questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204303: { // Nina
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							else if (var == 7) {
								return sendQuestDialog(env, 3398);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 7, 7, true, false); // reward
						}
					}
					break;
				}
				case 204399: { // Orhe
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							if (var == 1) {
								return defaultCloseDialog(env, 1, 2); // 2
							}
						}
					}
					break;
				}
				case 204332: { // Jorund
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							else if (var == 3) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO3: {
								return defaultCloseDialog(env, 2, 3, 182215364, 1, 0, 0); // 3
						}
						case SETPRO4: {
							player.getTitleList().addTitle(58, true, 0);
							return defaultCloseDialog(env, 3, 4); // 4
						}
					}
					break;
				}
				case 802047: { // Landver
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
						}
						case SETPRO6: {
							player.getTitleList().addTitle(58, true, 0);
							return defaultCloseDialog(env, 5, 6); // 6
						}
					}
					break;
				}
				case 700246: { // Dead Fire
					if (dialog == DialogAction.USE_OBJECT) {
						if (var == 3 || var == 4) {
							if (player.getInventory().getItemCountByItemId(182215365) > 0) {
								final Npc npc = (Npc) env.getVisibleObject();
								QuestService.addNewSpawn(220020000, 1, 204417, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
                                changeQuestStep(env, 3, 4, false);
								removeQuestItem(env, 182215364, 1);
								removeQuestItem(env, 182215365, 1);
							}
						}
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204301) { // Aegir
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (player.isInsideZone(ZoneName.get("ALTAR_OF_TRIAL_220020000"))) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 3, 4, false));
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int targetId = env.getTargetId();
			switch (targetId) {
				case 204417: {
					return defaultOnKillEvent(env, 204417, 4, 5); // 5
				}
				case 212877: {
					return defaultOnKillEvent(env, 212877, 6, 7); // 7
				}
			}
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 24020, true);
	}
}
