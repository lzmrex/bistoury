package qunar.tc.bistoury.attach.arthas.profiler;

import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.logger.Logger;
import qunar.tc.bistoury.attach.arthas.util.TypeResponseResult;
import qunar.tc.bistoury.attach.common.BistouryLoggerHelper;
import qunar.tc.bistoury.attach.common.BistouryLoggger;
import qunar.tc.bistoury.common.BistouryConstants;
import qunar.tc.bistoury.common.JacksonSerializer;
import qunar.tc.bistoury.common.TypeResponse;
import qunar.tc.bistoury.common.URLCoder;
import qunar.tc.bistoury.instrument.client.profiler.AgentProfilerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cai.wen created on 2019/10/25 11:17
 */
@Name(BistouryConstants.REQ_PROFILER_STATE_SEARCH)
public class ProfilerStateSearchCommand extends AnnotatedCommand {

    private static final Logger logger = BistouryLoggger.getLogger();

    private String id;

    private String type;

    @Argument(index = 0, argName = "id")
    public void setId(String id) {
        this.id = id;
    }

    @Argument(index = 1, argName = "type")
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void process(CommandProcess process) {
        logger.info("", "receive profiler search command, id: {}", id);
        Map<String, String> result = new HashMap<>();
        result.put("profilerId", id);
        TypeResponse typeResponse = TypeResponseResult.create(result, BistouryConstants.REQ_PROFILER_STATE_SEARCH);

        ProfilerClient profilerClient = ProfilerClients.getInstance();
        try {
            if (BistouryConstants.REQ_PROFILER_START_STATE_SEARCH.equals(type)) {
                result.put("status", profilerClient.status(id));
                result.put("type", BistouryConstants.REQ_PROFILER_START_STATE_SEARCH);
            } else if (BistouryConstants.REQ_PROFILER_FINNSH_STATE_SEARCH.equals(type)) {
                result.put("type", BistouryConstants.REQ_PROFILER_FINNSH_STATE_SEARCH);
                result.put("status", profilerClient.status(id));
            }
            typeResponse.getData().setCode(0);

        } catch (Exception e) {
            typeResponse.getData().setCode(-1);
            logger.error("", BistouryLoggerHelper.formatMessage("get state for id: {} error.", id), e);
        } finally {
            process.write(URLCoder.encode(JacksonSerializer.serialize(typeResponse)));
            process.end();
        }
    }

    private boolean isFinished(String profilerId) {
        return profilerId == null ||
                !profilerId.equals(AgentProfilerContext.getProfilerId())
                || !AgentProfilerContext.isProfiling();
    }
}
