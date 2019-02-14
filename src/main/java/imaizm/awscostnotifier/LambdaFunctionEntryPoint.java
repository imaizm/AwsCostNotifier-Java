package imaizm.awscostnotifier;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class LambdaFunctionEntryPoint implements RequestHandler<ScheduledEvent, String>  {
	public String handleRequest(ScheduledEvent event, Context context) {
		
		AmazonCloudWatch cloudWatch = AmazonCloudWatchClientBuilder.defaultClient();
		
		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest();
		request.setNamespace("AWS/Billing");
		request.setMetricName("EstimatedCharges");
		request.setDimensions(Arrays.asList(
			new Dimension().withName("Currency").withValue("USD")));
		request.setStartTime(
			Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
		request.setEndTime(
			Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		request.setPeriod(86400);
		request.setStatistics(Arrays.asList(
			"Maximum"));
		
		GetMetricStatisticsResult result = cloudWatch.getMetricStatistics(request);
		List<Datapoint> datapointList = result.getDatapoints();
		if (datapointList.size() != 1) {
			throw new Error();
		}
		Datapoint targetDatapoint = datapointList.get(0);
		
		Date datapointTimestamp = targetDatapoint.getTimestamp();
		Double datapointMaximum = targetDatapoint.getMaximum();
		
		context.getLogger().log("logger out sample");
		
		return "done";
	}
}
