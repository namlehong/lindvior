package l2s.gameserver.network.l2.s2c;

/**
 * @author VISTALL
 */
public class ExReplyRegisterDominion extends L2GameServerPacket
{
	public ExReplyRegisterDominion()
	{
		//
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0x92);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
	}
}