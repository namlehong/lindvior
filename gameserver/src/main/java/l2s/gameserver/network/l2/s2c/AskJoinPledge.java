package l2s.gameserver.network.l2.s2c;

public class AskJoinPledge extends L2GameServerPacket
{
	private int _requestorId;
	private String _pledgeName;

	public AskJoinPledge(int requestorId, String pledgeName)
	{
		_requestorId = requestorId;
		_pledgeName = pledgeName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x2c);
		writeD(_requestorId);
		writeS(_pledgeName);
	}
}