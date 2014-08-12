package l2s.gameserver.model.entity.events.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage2;

/**
 * @author VISTALL
 * @date 18:26/03.03.2011
 */
public class ClanHallNpcSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
	public ClanHallNpcSiegeEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void startEvent()
	{
		if(!_isInProgress.compareAndSet(false, true))
			return;

		_oldOwner = getResidence().getOwner();

		broadcastInZone(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()));

		super.startEvent();
	}

	@Override
	public void stopEvent(boolean step)
	{
		if(!_isInProgress.compareAndSet(true, false))
			return;

		Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			if(_oldOwner != newOwner)
			{
				newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);

				newOwner.incReputation(1700, false, toString());

				if(_oldOwner != null)
					_oldOwner.incReputation(-1700, false, toString());
			}

			broadcastInZone(new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName()).addResidenceName(getResidence()));
			broadcastInZone(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()));
		}
		else
			broadcastInZone(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));

		super.stopEvent(step);

		_oldOwner = null;
	}

	@Override
	public void processStep(Clan clan)
	{
		if(clan != null)
			getResidence().changeOwner(clan);

		stopEvent(true);
	}

	@Override
	public void loadSiegeClans()
	{
		//
	}
}
