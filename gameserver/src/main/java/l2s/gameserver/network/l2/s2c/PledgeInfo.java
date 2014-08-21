package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.pledge.Clan;

public class PledgeInfo extends L2GameServerPacket
{
	private int clan_id;
	private String clan_name, ally_name;

	public PledgeInfo(Clan clan)
	{
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		ally_name = clan.getAlliance() == null ? "" : clan.getAlliance().getAllyName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x89);
		writeD(Config.REQUEST_ID);
		writeD(clan_id);
		writeS(clan_name);
		writeS(ally_name);
	}
}