package l2s.gameserver.network.l2.s2c;

/**
 *
 * @author monithly
 */
public class ExLightingCandleEvent extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeEx(0x11D);
	}
}
