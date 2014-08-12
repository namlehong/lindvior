package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFail;
import l2s.gameserver.tables.ClanTable;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS(32);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan playerClan = activeChar.getClan();
		if(playerClan == null)
			return;

		if(!((activeChar.getClanPrivileges() & Clan.CP_CL_CLAN_WAR) == Clan.CP_CL_CLAN_WAR))
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT, ActionFail.STATIC);
			return;
		}

		Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

		if(clan == null)
		{
			activeChar.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID, ActionFail.STATIC);
			return;
		}

		if(!playerClan.isAtWarWith(clan.getClanId()))
		{
			//activeChar.sendPacket(SystemMsg.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_AGAINST_THE_CLAN_S1, ActionFail.STATIC); [TODO] FIX this S1 incorect method.
			return;
		}

		for(UnitMember mbr : playerClan)
			if(mbr.isOnline() && mbr.getPlayer().isInCombat())
			{
				activeChar.sendPacket(SystemMsg.A_CEASEFIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE, ActionFail.STATIC);
				return;
			}

		ClanTable.getInstance().stopClanWar(playerClan, clan);
	}
}