package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage2;

public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	// format: (ch)dSdS
	int _replace;
	String _subjectName;
	int _targetUnit;
	String _replaceName;

	@Override
	protected void readImpl()
	{
		_replace = readD();
		_subjectName = readS(16);
		_targetUnit = readD();
		if(_replace > 0)
			_replaceName = readS();
	}

	@Override
	protected void runImpl()
	{
		//_log.warn("Received RequestPledgeReorganizeMember("+_arg1+","+_arg2+","+_arg3+","+_arg4+") from player "+getClient().getActiveChar().getName());

		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(!activeChar.isClanLeader())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.ChangeAffiliations", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		UnitMember subject = clan.getAnyMember(_subjectName);
		if(subject == null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.NotInYourClan", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(subject.getPledgeType() == _targetUnit)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.AlreadyInThatCombatUnit", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(_targetUnit != 0 && clan.getSubUnit(_targetUnit) == null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.NoSuchCombatUnit", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		if(Clan.isAcademy(_targetUnit))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.AcademyViaInvitation", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		/*
		 * unsure for next check, but anyway as workaround before academy refactoring
		 * (needs LvlJoinedAcademy to be put on UnitMember if so, to be able relocate from academy correctly)
		 */
		if(Clan.isAcademy(subject.getPledgeType()))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.CantMoveAcademyMember", activeChar));
			activeChar.sendActionFailed();
			return;
		}

		UnitMember replacement = null;

		if(_replace > 0)
		{
			replacement = clan.getAnyMember(_replaceName);
			if(replacement == null)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.CharacterNotBelongClan", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			if(replacement.getPledgeType() != _targetUnit)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.CharacterNotBelongCombatUnit", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			if(replacement.isSubLeader() != 0)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.CharacterLeaderAnotherCombatUnit", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			if(clan.getUnitMembersSize(_targetUnit) >= clan.getSubPledgeLimit(_targetUnit))
			{
				if(_targetUnit == Clan.SUBUNIT_MAIN_CLAN)
					activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME).addString(clan.getName()));
				else
					activeChar.sendPacket(SystemMsg.THE_ACADEMYROYAL_GUARDORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME);
				activeChar.sendActionFailed();
				return;
			}

			if(subject.isSubLeader() != 0)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.clientpackets.RequestPledgeReorganizeMember.MemberLeaderAnotherUnit", activeChar));
				activeChar.sendActionFailed();
				return;
			}

		}

		SubUnit oldUnit = null;

		if(replacement != null)
		{
			oldUnit = replacement.getSubUnit();

			oldUnit.replace(replacement.getObjectId(), subject.getPledgeType());

			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(replacement));

			if(replacement.isOnline())
			{
				replacement.getPlayer().updatePledgeRank();
				replacement.getPlayer().broadcastCharInfo();
			}
		}

		oldUnit = subject.getSubUnit();

		oldUnit.replace(subject.getObjectId(), _targetUnit);

		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subject));

		if(subject.isOnline())
		{
			subject.getPlayer().updatePledgeRank();
			subject.getPlayer().broadcastCharInfo();
		}
	}
}