package l2s.gameserver.model.instances;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.MyTargetSelected;
import l2s.gameserver.network.l2.s2c.ValidateLocation;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class OlympiadBufferInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private TIntHashSet buffs = new TIntHashSet();

	public OlympiadBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
		{
			player.sendActionFailed();
			return;
		}

		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(player, this));
			if(!isInRange(player, INTERACTION_DISTANCE))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else if(buffs.size() > 4)
				showChatWindow(player, 1);
			else
				showChatWindow(player, 0);
			player.sendActionFailed();
		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(buffs.size() > 4)
			showChatWindow(player, 1);

		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();
		if(cmd.equalsIgnoreCase("buff"))
		{
			int id = Integer.parseInt(st.nextToken());
			int lvl = Integer.parseInt(st.nextToken());
			boolean checked = false;
			for(int[] buff : Olympiad.BUFFS_LIST)
			{
				if(buff.length != 2)
					continue;

				if(buff[0] != id)
					continue;

				if(buff[1] != lvl)
					continue;

				checked = true;
				break;
			}

			if(!checked)
			{
				//Читер!
				showChatWindow(player, 0);
				return;
			}

			Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			List<Creature> target = new ArrayList<Creature>();
			target.add(player);
			broadcastPacket(new MagicSkillUse(this, player, id, lvl, 0, 0));
			callSkill(skill, target, true);
			buffs.add(id);
			if(buffs.size() > 4)
				showChatWindow(player, 1);
			else
				showChatWindow(player, 0);
		}
		else
			showChatWindow(player, 0);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "buffer";
		else
			pom = "buffer-" + val;

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "olympiad/" + pom + ".htm";
	}
}