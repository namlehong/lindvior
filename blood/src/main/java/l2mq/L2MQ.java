package l2mq;

import l2mq.callback.ChatterSay;
import l2mq.callback.MQMailer;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;

import org.gearman.Gearman;
import org.gearman.GearmanClient;
import org.gearman.GearmanServer;
import org.gearman.GearmanWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blood.Blood;

public class L2MQ
{
	@SuppressWarnings("unused")
	private static final Logger 		_log 			= LoggerFactory.getLogger(L2MQ.class);
	private static L2MQ 				_instance;
	
	private static Gearman 				_gearman;
	private static GearmanClient 		_client;
	private static GearmanServer 		_server;
	private static GearmanWorker		_worker;
		
	public static L2MQ getInstance() {
        if (_instance == null) {
        	
            _instance =
                new L2MQ();
        }
        return _instance;
    }
	
	private L2MQ() {
		if(!Blood.MQ_ENABLE)
			return;
		getWorker(); // set worker
	}
	
	public static Gearman getGearman()
	{
		if(_gearman == null)
		{
			_gearman = Gearman.createGearman();
		}
		
		return _gearman;
	}
	
	public static GearmanServer getGearmanServer()
	{
		if(_server == null)
		{
			_server = getGearman().createGearmanServer(Blood.MQ_SERVER, Blood.MQ_PORT);
		}
		
		return _server;
	}
	
	public static GearmanClient getClient()
	{
		if(_client == null)
		{
			_client = getGearman().createGearmanClient();
			_client.addServer(getGearmanServer());
		}
		
		return _client;
	}
	
	public static GearmanWorker getWorker()
	{
		if(_worker == null)
		{
			_worker = getGearman().createGearmanWorker();
			_worker.addServer(getGearmanServer());
			asignTaskForWorker(_worker);
		}
		
		return _worker;
	}
	
	public static void asignTaskForWorker(GearmanWorker worker)
	{
		worker.addFunction("gameMail_"+Blood.MQ_PREFIX, new MQMailer());
		worker.addFunction("gameSay_"+Blood.MQ_PREFIX, new ChatterSay());
	}
	
	
	public static class JobResult
	{
		public boolean result = false;
		public String msg;
	}
	
	public static void chat(Player receiver, ChatType chat_type, String sender, String msg)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(receiver.getAccountName());
		builder.append(";");
		builder.append(receiver.getName());
		builder.append(";");
		builder.append(chat_type.ordinal());
		builder.append(";");
		builder.append(sender);
		builder.append(";");
		builder.append(msg);
		
		_log.info("PM to FPC:"+builder.toString());
		
		addBackgroundJob("chat", builder.toString());
	}
	
	public static void addBackgroundJob(String jobName, String jobData)
	{
		if(!Blood.MQ_ENABLE)
			return;
		jobData = Blood.MQ_PREFIX +";" + jobData;
		System.out.println(jobName+"|"+jobData);
		getClient().submitBackgroundJob(jobName, jobData.getBytes());
	}
	
}