package l2s.gameserver.network.l2.s2c;

/**
 * Format: (chd)
 */
public class ExCubeGameCloseUI extends L2GameServerPacket
{
	int _seconds;

	public ExCubeGameCloseUI()
	{}

	@Override
	protected void writeImpl()
	{
		writeEx(0x98);
		writeD(0xffffffff);
	}
}