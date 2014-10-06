package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.InvisibleType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;

public class UserInfo extends L2GameServerPacket
{
	private boolean can_writeImpl = false, partyRoom;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd, _relation;
	private double move_speed, attack_speed, col_radius, col_height;
	private int[][] _inv;
	private Location _loc, _fishLoc;
	private int obj_id, vehicle_obj_id, _race, sex, base_class, level, curCp, maxCp, _enchant, _weaponFlag;
	private long _exp;
	private int curHp, maxHp, curMp, maxMp, curLoad, maxLoad, rec_left, rec_have;
	private int _str, _con, _dex, _int, _wit, _men, _sp, ClanPrivs, InventoryLimit;
	private int _patk, _patkspd, _pdef, _matk, _matkspd;
	private int _pEvasion, _pAccuracy, _pCrit, _mEvasion, _mAccuracy, _mCrit;
	private int _mdef, pvp_flag, karma, hair_style, hair_color, face, gm_commands, fame, vitality;
	private int clan_id, clan_crest_id, ally_id, ally_crest_id, large_clan_crest_id;
	private int private_store, can_crystalize, pk_kills, pvp_kills, class_id, agathion, _partySubstitute;
	private int noble, hero, mount_id, cw_level;
	private int name_color, running, pledge_class, pledge_type, title_color, transformation;
	private int defenceFire, defenceWater, defenceWind, defenceEarth, defenceHoly, defenceUnholy;
	private int mount_type;
	private String _name, title;
	private EffectCubic[] cubics;
	private Element attackElement;
	private int attackElementValue;
	private boolean isFlying, _allowMap;
	private int talismans;
	private boolean openCloak;
	private double _expPercent;
	private TeamType _team;
	private AbnormalEffect[] _abnormalEffects;

	public UserInfo(Player player)
	{
		if(player.isCursedWeaponEquipped())
		{
			_name = player.getCursedWeaponName(player);

			title = "";
			clan_crest_id = 0;
			ally_crest_id = 0;
			large_clan_crest_id = 0;
			cw_level = CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId());
		}
		else
		{
			_name = player.getName();

			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId();
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

			cw_level = 0;
			title = player.getTitle();
		}

		if(player.getPlayerAccess().GodMode && player.getInvisibleType() == InvisibleType.GM)
			title += "[I]";
		if(player.isPolymorphed())
			if(NpcHolder.getInstance().getTemplate(player.getPolyId()) != null)
				title += " - " + NpcHolder.getInstance().getTemplate(player.getPolyId()).name;
			else
				title += " - Polymorphed";

		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType().ordinal();
		}
		else
		{
			_enchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		_weaponFlag = player.getActiveWeaponInstance() == null ? 0x14 : 0x28;

		move_speed = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / move_speed);
		_walkSpd = (int) (player.getWalkSpeed() / move_speed);

		_flRunSpd = 0; // TODO
		_flWalkSpd = 0; // TODO

		if(player.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}

		_swimRunSpd = player.getSwimRunSpeed();
		_swimWalkSpd = player.getSwimWalkSpeed();

		_inv = new int[Inventory.PAPERDOLL_MAX][5];
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = player.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][4] = player.getInventory().getPaperdollVisualId(PAPERDOLL_ID);
		}

		if(player.getClan() != null)
		{
			_relation |= RelationChanged.USER_RELATION_CLAN_MEMBER;
			if(player.isClanLeader())
				_relation |= RelationChanged.USER_RELATION_CLAN_LEADER;
		}

		for(GlobalEvent e : player.getEvents())
			_relation = e.getUserRelation(player, _relation);

		_loc = player.getLoc();
		obj_id = player.getObjectId();
		vehicle_obj_id = player.isInBoat() ? player.getBoat().getBoatId() : 0x00;
		_race = player.getRace().ordinal();
		sex = player.getSex().ordinal();
		base_class = ClassId.VALUES[player.getBaseDefaultClassId()].getFirstParent(sex).getId();
		level = player.getLevel();
		_exp = player.getExp();
		_expPercent = Experience.getExpPercent(player.getLevel(), player.getExp());
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curMp = (int) player.getCurrentMp();
		maxMp = player.getMaxMp();
		curLoad = player.getCurrentLoad();
		maxLoad = player.getMaxLoad();
		_sp = player.getIntSp();
		_patk = player.getPAtk(null);
		_patkspd = player.getPAtkSpd();
		_pdef = player.getPDef(null);
		_pEvasion = player.getPEvasionRate(null);
		_pAccuracy = player.getPAccuracy();
		_pCrit = player.getPCriticalHit(null);
		_mEvasion = player.getMEvasionRate(null);
		_mAccuracy = player.getMAccuracy();
		_mCrit = player.getMCriticalHit(null, null);
		_matk = player.getMAtk(null, null);
		_matkspd = player.getMAtkSpd();
		_mdef = player.getMDef(null, null);
		pvp_flag = player.getPvpFlag(); // 0=white, 1=purple, 2=purpleblink
		karma = player.getKarma();
		attack_speed = player.getAttackSpeedMultiplier();
		col_radius = player.getCollisionRadius();
		col_height = player.getCollisionHeight();
		hair_style = player.getBeautyHairStyle() > 0 ? player.getBeautyHairStyle() : player.getHairStyle();
		hair_color = player.getBeautyHairColor() > 0 ? player.getBeautyHairColor() : player.getHairColor();
		face = player.getBeautyFace() > 0 ? player.getBeautyFace() : player.getFace();
		gm_commands = player.isGM() || player.getPlayerAccess().CanUseAltG ? 1 : 0;
		// builder level активирует в клиенте админские команды
		clan_id = player.getClanId();
		ally_id = player.getAllyId();
		private_store = player.getPrivateStoreType();
		can_crystalize = player.getSkillLevel(Skill.SKILL_CRYSTALLIZE) > 0 ? 1 : 0;
		pk_kills = player.getPkKills();
		pvp_kills = player.getPvpKills();
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		_abnormalEffects = player.getAbnormalEffectsArray();
		ClanPrivs = player.getClanPrivileges();
		rec_left = player.getRecomLeft(); //c2 recommendations remaining
		rec_have = player.getRecomHave(); //c2 recommendations received
		InventoryLimit = player.getInventoryLimit();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCp = (int) player.getCurrentCp();
		_team = player.getTeam();
		noble = player.isNoble() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0; //0x01: symbol on char menu ctrl+I
		hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0; //0x01: Hero Aura and symbol
		//fishing = _cha.isFishing() ? 1 : 0; // Fishing Mode
		_fishLoc = player.getFishLoc();
		name_color = player.getNameColor();
		running = player.isRunning() ? 0x01 : 0x00; //changes the Speed display on Status Window
		pledge_class = player.getPledgeRank().ordinal();
		pledge_type = player.getPledgeType();
		title_color = player.getTitleColor();
		transformation = player.getVisualTransformId();
		attackElement = player.getAttackElement();
		attackElementValue = player.getAttack(attackElement);
		defenceFire = player.getDefence(Element.FIRE);
		defenceWater = player.getDefence(Element.WATER);
		defenceWind = player.getDefence(Element.WIND);
		defenceEarth = player.getDefence(Element.EARTH);
		defenceHoly = player.getDefence(Element.HOLY);
		defenceUnholy = player.getDefence(Element.UNHOLY);
		agathion = player.getAgathionId();
		fame = player.getFame();
		vitality = player.getVitality();
		partyRoom = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		isFlying = player.isInFlyingTransform();
		talismans = player.getTalismanCount();
		openCloak = player.getOpenCloak();
		_allowMap = player.isActionBlocked(Zone.BLOCKED_ACTION_MINIMAP);
		_partySubstitute = player.isPartySubstituteStarted()  ? 1 : 0;

		can_writeImpl = true;
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_writeImpl)
			return;

		writeC(0x32);

		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(vehicle_obj_id);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(sex);
		writeD(base_class);
		writeD(level);
		writeQ(_exp);
		writeF(_expPercent);
		writeD(_str);
		writeD(_dex);
		writeD(_con);
		writeD(_int);
		writeD(_wit);
		writeD(_men);
		writeD(maxHp);
		writeD(curHp);
		writeD(maxMp);
		writeD(curMp);
		writeD(_sp);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(_weaponFlag);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][0]);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][1]);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			writeH(_inv[PAPERDOLL_ID][2]);
			writeH(_inv[PAPERDOLL_ID][3]);
		}

		writeD(talismans);
		writeD(openCloak ? 0x01 : 0x00);

		writeD(_inv[Inventory.PAPERDOLL_RHAND][4]); //Внешний вид оружия (ИД Итема).
		writeD(_inv[Inventory.PAPERDOLL_LHAND][4]); //Внешний вид щита (ИД Итема).
		writeD(0x00); //???
		writeD(_inv[Inventory.PAPERDOLL_GLOVES][4]); //Внешний вид перчаток (ИД Итема).
		writeD(_inv[Inventory.PAPERDOLL_CHEST][4]); //Внешний вид верха (ИД Итема).
		writeD(_inv[Inventory.PAPERDOLL_LEGS][4]); //Внешний вид низа (ИД Итема).
		writeD(_inv[Inventory.PAPERDOLL_FEET][4]); //Внешний вид ботинок (ИД Итема).
		writeD(_inv[Inventory.PAPERDOLL_HAIR][4]); //Внешний вид шляпы (ИД итема).
		writeD(_inv[Inventory.PAPERDOLL_DHAIR][4]); //Внешний вид маски (ИД итема).

		writeD(_patk);
		writeD(_patkspd);
		writeD(_pdef);
		writeD(_pEvasion);
		writeD(_pAccuracy);
		writeD(_pCrit);
		writeD(_matk);
		writeD(_matkspd);
		writeD(_patkspd);
		writeD(_mEvasion);
		writeD(_mdef);
		writeD(_mAccuracy);
		writeD(_mCrit);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd); // swimspeed
		writeD(_swimWalkSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);
		writeS(title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(mount_type); // mount type
		writeC(private_store);
		writeC(can_crystalize);
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom ? 0x01 : 0x00); //1-find party members
		writeC(isFlying ? 0x02 : 0x00);
		writeD(ClanPrivs);
		writeH(rec_left);
		writeH(rec_have);
		writeD(mount_id);
		writeH(InventoryLimit);
		writeD(class_id);
		writeD(0x00); // special effects? circles around player...
		writeD(maxCp);
		writeD(curCp);
		writeC(_enchant);
		writeC(_team.ordinal());
		writeD(large_clan_crest_id);
		writeC(noble);
		writeC(hero);
		writeC(0x00);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);
		writeD(name_color);
		writeC(running);
		writeD(pledge_class);
		writeD(pledge_type);
		writeD(title_color);
		writeD(cw_level);
		writeD(transformation); // Transformation id

		// AttackElement (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None)
		writeH(attackElement.getId());
		writeH(attackElementValue); // AttackElementValue
		writeH(defenceFire); // DefAttrFire
		writeH(defenceWater); // DefAttrWater
		writeH(defenceWind); // DefAttrWind
		writeH(defenceEarth); // DefAttrEarth
		writeH(defenceHoly); // DefAttrHoly
		writeH(defenceUnholy); // DefAttrUnholy

		writeD(agathion);

		// T2 Starts
		writeD(fame); // Fame
		writeD(_allowMap ? 1 : 0); // Minimap on Hellbound

		writeD(vitality); // Vitality Points

		writeD(0x00);// Unknown GOD
		writeD(_partySubstitute);// Unknown GOD (1 - Party searching?)
		writeC(0x00);// Unknown GOD
		writeD(0x00); // TAUTI

		writeD(_abnormalEffects.length);
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeD(abnormal.ordinal());

		writeC(0x00); // TAUTI
	}
}