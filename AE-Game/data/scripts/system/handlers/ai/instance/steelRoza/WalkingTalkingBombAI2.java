package ai.instance.steelRoza;

import ai.AggressiveNpcAI2;
import com.aionemu.commons.network.util.ThreadPoolManager;
import org.typezero.gameserver.ai2.AI2Actions;
import org.typezero.gameserver.ai2.AIName;
import org.typezero.gameserver.ai2.poll.AIAnswer;
import org.typezero.gameserver.ai2.poll.AIAnswers;
import org.typezero.gameserver.ai2.poll.AIQuestion;
import org.typezero.gameserver.skillengine.SkillEngine;

/**
 *
 * @author xXMashUpXx
 *
 */
@AIName("walkingtalkingbomb")
public class WalkingTalkingBombAI2 extends AggressiveNpcAI2 {

    @Override
    protected void handleSpawned() {
        super.handleSpawned();
        doSchedule();
    }

    private void doSchedule() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (!isAlreadyDead()) {
                    SkillEngine.getInstance().getSkill(getOwner(), 19416, 49, getOwner()).useNoAnimationSkill();
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (!isAlreadyDead()) {
                                despawn();
                            }
                        }
                    }, 4000);
                }
            }
        }, 2000);
    }

    @Override
    public AIAnswer ask(AIQuestion question) {
        switch (question) {
            case CAN_RESIST_ABNORMAL:
                return AIAnswers.POSITIVE;
            default:
                return AIAnswers.NEGATIVE;
        }
    }

    @Override
    protected AIAnswer pollInstance(AIQuestion question) {
        switch (question) {
            case SHOULD_DECAY:
                return AIAnswers.NEGATIVE;
            case SHOULD_RESPAWN:
                return AIAnswers.NEGATIVE;
            case SHOULD_REWARD:
                return AIAnswers.NEGATIVE;
            default:
                return null;
        }
    }

    private void despawn() {
        AI2Actions.deleteOwner(this);
    }
}
