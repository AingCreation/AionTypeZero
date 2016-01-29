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

package org.typezero.gameserver.services.antihack;

import org.typezero.gameserver.configs.main.SecurityConfig;
import org.typezero.gameserver.controllers.movement.MovementMask;
import org.typezero.gameserver.controllers.movement.PlayerMoveController;
import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.network.aion.AionServerPacket;
import org.typezero.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import org.typezero.gameserver.network.aion.serverpackets.SM_MOVE;
import org.typezero.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import org.typezero.gameserver.skillengine.effect.AbnormalState;
import org.typezero.gameserver.utils.MathUtil;
import org.typezero.gameserver.utils.PacketSendUtility;
import org.typezero.gameserver.utils.audit.AuditLogger;
import org.typezero.gameserver.world.World;

/**
 * @author Source
 */
public class AntiHackService {

	public static boolean canMove(Player player, float x, float y, float z, float speed, byte type) {
		AionServerPacket forcedMove = new SM_FORCED_MOVE(player, player.getObjectId(), x, y, z);
		AionServerPacket normalMove = new SM_MOVE(player);

		if (SecurityConfig.ABNORMAL) {
			if (!player.canPerformMove() && !player.getEffectController().isAbnormalSet(AbnormalState.CANNOT_MOVE) && (type & MovementMask.GLIDE) != MovementMask.GLIDE) {
				if (player.abnormalHackCounter > SecurityConfig.ABNORMAL_COUNTER) {
					punish(player, x, y, type, forcedMove, "Detected illegal Action (Anti-Abnormal Hack)");
					return false;
				}
				else
					player.abnormalHackCounter++;
			}
			else
				player.abnormalHackCounter = 0;
		}

		if (SecurityConfig.SPEEDHACK) {
			if (type != 0) {
				if (type == -64 || type == -128) {
					PlayerMoveController m = player.getMoveController();
					double vector2D = MathUtil.getDistance(x, y, m.getTargetX2(), m.getTargetY2());

					if (vector2D != 0) {
						if (type == -64 && vector2D > 5 && vector2D > speed + 0.001)
							player.speedHackCounter++;
						else if (vector2D > 37.5 && vector2D > 1.5 * speed * speed + 0.001)
							player.speedHackCounter++;
						else if (player.speedHackCounter > 0)
							player.speedHackCounter--;

						if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER) {
							return punish(player, x, y, type, forcedMove, "Detected illegal action (Speed Hack)"
									+ " SHC:" + player.speedHackCounter
									+ " S:" + speed
									+ " V:" + Math.rint(1000.0 * vector2D) / 1000.0
									+ " type:" + type);
						}
					}
				}
				else if ((type & MovementMask.MOUSE) == MovementMask.MOUSE && (type & MovementMask.GLIDE) != MovementMask.GLIDE) {
					double vector = MathUtil.getDistance(x, y, player.prevPos.getX(), player.prevPos.getY());
					long timeDiff = System.currentTimeMillis() - player.prevPosUT;

					if ((type & MovementMask.STARTMOVE) == MovementMask.STARTMOVE) {
						boolean isMoveToTarget = false;
						if (player.getTarget() != null && player.getTarget() != player) {
							PlayerMoveController m = player.getMoveController();
							double distDiff = MathUtil.getDistance(Math.round(player.getTarget().getX()), Math.round(player.getTarget().getY()), Math.round(m.getTargetX2()), Math.round(m.getTargetY2()));
							isMoveToTarget = distDiff <= 5;
						}

						if (timeDiff > 1000 && player.speedHackCounter > 0)
							player.speedHackCounter--;

						if (vector > timeDiff * (speed + 0.85) * 0.001)
							player.speedHackCounter++;
						else if (isMoveToTarget && player.speedHackCounter > 0)
							player.speedHackCounter--;
					}
					else if (vector > timeDiff * (speed + 0.25) * 0.001)
						player.speedHackCounter++;
					else if (player.speedHackCounter > 0)
						player.speedHackCounter--;

					if (SecurityConfig.PUNISH > 0 && player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER + 5) {
						return punish(player, x, y, type, forcedMove, "Detected illegal action (Speed Hack)"
								+ " SHC:" + player.speedHackCounter
								+ " SMS:" + Math.rint(100.0 * (timeDiff * (speed + 0.25) * 0.001)) / 100.0
								+ " TDF:" + timeDiff
								+ " VTD:" + Math.rint(1000.0 * (timeDiff * (speed + 0.85) * 0.001)) / 1000.0
								+ " VS:" + Math.rint(100.0 * vector) / 100.0
								+ " type:" + type);
					}
					else if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER) {
						moveBack(player, x, y, type, forcedMove);
						return false;
					}
				}
			}
			else {
				double vector = MathUtil.getDistance(x, y, player.prevPos.getX(), player.prevPos.getY());
				long timeDiff = System.currentTimeMillis() - player.prevPosUT;

				if (player.prevMoveType == 0 && vector > timeDiff * speed * 0.00075)
					player.speedHackCounter++;

				if (SecurityConfig.PUNISH > 0 && player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER + 5) {
					return punish(player, x, y, type, forcedMove, "Detected illegal action (Speed Hack)"
							+ " SHC:" + player.speedHackCounter
							+ " TD:" + Math.rint(1000.0 * timeDiff) / 1000.0
							+ " VTD:" + Math.rint(1000.0 * (timeDiff * speed * 0.00075)) / 1000.0
							+ " VS:" + Math.rint(100.0 * vector) / 100.0
							+ " type:" + type);
				}
				else if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER + 2) {
					moveBack(player, x, y, type, forcedMove);
					return false;
				}
			}

			// Store prev. move info
			player.prevPos.setXYZH(x, y, z, player.getHeading());
			player.prevPosUT = System.currentTimeMillis();
			if (player.prevMoveType != type)
				player.prevMoveType = type;
		}

		if (SecurityConfig.TELEPORTATION) {
			double delta = MathUtil.getDistance(x, y, player.getX(), player.getY()) / speed;
			if (speed > 5.0 && delta > 5.0 && (type & MovementMask.GLIDE) != MovementMask.GLIDE) {
				World.getInstance().updatePosition(player, player.getX(), player.getY(), player.getZ(), player.getHeading());
				return punish(player, x, y, type, normalMove, "Detected illegal action (Teleportation)"
						+ " S:" + speed
						+ " D:" + Math.rint(1000.0 * delta) / 1000.0
						+ " type:" + type);
			}
		}

		return true;
	}

	protected static boolean punish(Player player, float x, float y, byte type, AionServerPacket pkt, String message) {
		switch (SecurityConfig.PUNISH) {
			case 1:
				AuditLogger.info(player, message);
				moveBack(player, x, y, type, pkt);
				return false;
			case 2:
				AuditLogger.info(player, message);
				moveBack(player, x, y, type, pkt);
				if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER * 3
						|| player.abnormalHackCounter > SecurityConfig.ABNORMAL_COUNTER * 3)
					player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
				return false;
			case 3:
				AuditLogger.info(player, message);
				player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
				return false;
			default:
				AuditLogger.info(player, message);
				return true;
		}
	}

	protected static void moveBack(Player player, float x, float y, byte type, AionServerPacket pkt) {
		PacketSendUtility.broadcastPacketAndReceive(player, pkt);
		player.getMoveController().updateLastMove();
		player.prevPos.setXYZH(x, y, 0f, (byte) 0);
		player.prevPosUT = System.currentTimeMillis();
		if (player.prevMoveType != type)
			player.prevMoveType = type;
	}

}