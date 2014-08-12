package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;

public class PartySmallWindowAll extends L2GameServerPacket
{
	private int leaderId, loot;
	private List<PartySmallWindowMemberInfo> members = new ArrayList<PartySmallWindowMemberInfo>();

	public PartySmallWindowAll(Party party, Player exclude)
	{
		leaderId = party.getPartyLeader().getObjectId();
		loot = party.getLootDistribution();

		for(Player member : party.getPartyMembers())
			if(member != exclude)
				members.add(new PartySmallWindowMemberInfo(member));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4E);
		writeD(leaderId); // c3 party leader id
		writeD(loot); //c3 party loot type (0,1,2,....)
		writeD(members.size());
		for(PartySmallWindowMemberInfo mi : members)
		{
			writeD(mi.member.objId);
			writeS(mi.member.name);
			writeD(mi.member.curCp);
			writeD(mi.member.maxCp);
			writeD(mi.member.curHp);
			writeD(mi.member.maxHp);
			writeD(mi.member.curMp);
			writeD(mi.member.maxMp);
			writeD(mi.member.vitality);
			writeD(mi.member.level);
			writeD(mi.member.classId);
			writeD(mi.member.sex);
			writeD(mi.member.raceId);
			writeD(0x00); // Hide Name
			writeD(0x00); // unk
			writeD(mi.member.isPartySubstituteStarted); // Ищется ли замена данному игроку.
			writeD(mi.m_servitors.size()); // Pet Count
			for(PartyMember servitor : mi.m_servitors)
			{
				writeD(servitor.type);
				writeD(servitor.objId);
				writeD(servitor.npcId);
				writeS(servitor.name);
				writeD(servitor.curHp);
				writeD(servitor.maxHp);
				writeD(servitor.curMp);
				writeD(servitor.maxMp);
				writeD(servitor.level);
			}
		}
	}

	public static class PartySmallWindowMemberInfo
	{
		public PartyMember member;
		public List<PartyMember> m_servitors;

		public PartySmallWindowMemberInfo(Player player)
		{
			member = new PartyMember();
			member.name = player.getName();
			member.objId = player.getObjectId();
			member.curCp = (int) player.getCurrentCp();
			member.maxCp = player.getMaxCp();
			member.curHp = (int) player.getCurrentHp();
			member.maxHp = player.getMaxHp();
			member.curMp = (int) player.getCurrentMp();
			member.maxMp = player.getMaxMp();
			member.level = player.getLevel();
			member.classId = player.getClassId().getId();
			member.raceId = player.getRace().ordinal();
			member.vitality = player.getVitality();
			member.sex = player.getSex().ordinal();
			member.isPartySubstituteStarted = player.isPartySubstituteStarted() ? 1 : 0;

			Servitor[] servitors = player.getServitors();
			m_servitors = new ArrayList<PartyMember>(servitors.length);
			if(servitors.length > 0)
			{
				for(Servitor s : servitors)
				{
					PartyMember m_servitor = new PartyMember();
					m_servitor.name = s.getName();
					m_servitor.objId = s.getObjectId();
					m_servitor.npcId = s.getNpcId() + 1000000;
					m_servitor.curHp = (int) s.getCurrentHp();
					m_servitor.maxHp = s.getMaxHp();
					m_servitor.curMp = (int) s.getCurrentMp();
					m_servitor.maxMp = s.getMaxMp();
					m_servitor.level = s.getLevel();
					m_servitor.type = s.getServitorType();
					m_servitors.add(m_servitor);
				}
			}
		}
	}

	public static class PartyMember
	{
		public String name;
		public int objId, npcId, curCp, maxCp, curHp, maxHp, curMp, maxMp, level, classId, raceId, vitality, type, sex, isPartySubstituteStarted;
	}
}