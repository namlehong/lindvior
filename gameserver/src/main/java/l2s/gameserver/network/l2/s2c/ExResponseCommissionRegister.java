package l2s.gameserver.network.l2.s2c;

public class ExResponseCommissionRegister extends L2GameServerPacket
{
	protected void writeImpl()
	{
		writeEx(0xF5);
		writeD(0x00);
		writeD(0x00);
		writeQ(0x00);
	}
}
