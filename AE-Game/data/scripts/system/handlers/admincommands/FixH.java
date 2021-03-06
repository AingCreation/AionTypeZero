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

package admincommands;

import org.typezero.gameserver.dataholders.DataManager;
import org.typezero.gameserver.model.gameobjects.Npc;
import org.typezero.gameserver.model.gameobjects.VisibleObject;
import org.typezero.gameserver.model.gameobjects.player.Player;
import org.typezero.gameserver.model.templates.spawns.SpawnTemplate;
import org.typezero.gameserver.spawnengine.SpawnEngine;
import org.typezero.gameserver.utils.PacketSendUtility;
import org.typezero.gameserver.utils.chathandlers.AdminCommand;
import java.io.IOException;


/**
 * @author Romanz.
 */
public class FixH extends AdminCommand
{
	private byte heading = 0;
	public FixH()	{ super("fixh"); }

@Override
public void execute(Player admin, String[] params)
{
    //if (admin.getAccessLevel() < AdminConfig.COMMAND_FIXH) {
        if (admin.getAccessLevel() < 1) {
		PacketSendUtility.sendMessage(admin, "You dont have enough rights to use this command!");
                return; }

    if (admin.getTarget() != null)
    {
        if(admin.getTarget() instanceof Npc)
	{
            Npc target = (Npc) admin.getTarget();
            final SpawnTemplate temp = target.getSpawn();
            int respawnTime = 295;
            boolean permanent = true;

            //count heading
            heading = admin.getHeading();
            if (heading > 60)
                heading -= 60;
            else
		heading += 60;

            //delete spawn,npc
            //DataManager.PLACEMENT_DATA.removeSpawn(temp);
            target.getController().delete();

            //spawn npc
            int templateId = temp.getNpcId();
            float x = temp.getX();
            float y = temp.getY();
            float z = temp.getZ();
            byte heading = admin.getHeading();
            int worldId = temp.getWorldId();

        SpawnTemplate spawn = SpawnEngine.addNewSpawn(worldId, templateId, x, y, z, heading, respawnTime);

        if (spawn == null) {
            PacketSendUtility.sendMessage(admin, "There is no template with id " + templateId);
            return;
        }

        VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, admin.getInstanceId());

        if (visibleObject == null) {
            PacketSendUtility.sendMessage(admin, "Spawn id " + templateId + " was not found!");
        } else if (permanent) {
            try {
                DataManager.SPAWNS_DATA2.saveSpawn(admin, visibleObject, false);
            } catch (IOException e) {
                PacketSendUtility.sendMessage(admin, "Could not save spawn");
            }
        }

        String objectName = visibleObject.getObjectTemplate().getName();
        PacketSendUtility.sendMessage(admin, objectName + " FixH");
    }

        }
            else { PacketSendUtility.sendMessage(admin, "Only instances of NPC are allowed as target!");
 }
    }

    protected VisibleObject spawn(int npcId, int mapId, int instanceId, float x, float y, float z, byte heading, String walkerId, int walkerIdx, int respawnTime) {
        SpawnTemplate template = SpawnEngine.addNewSpawn(mapId, npcId, x, y, z, heading, respawnTime);
        return SpawnEngine.spawnObject(template, instanceId);
    }

}
