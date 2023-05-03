package polis.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentGroup;
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.vk.api.VkAuthorizator;


@Component
public class CommandUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);
    @Autowired
    private OkDataCheck okDataCheck;

    @Autowired
    private VkDataCheck vkDataCheck;

    String getGroupName(CurrentAccount currentAccount, CurrentGroup currentGroup) {
        String groupName = null;
        switch (currentGroup.getSocialMedia()) {
            case OK -> groupName = okDataCheck.getOKGroupName(currentGroup.getGroupId(),
                    currentAccount.getAccessToken());
            case VK -> groupName = vkDataCheck.getVkGroupName(
                    new VkAuthorizator.TokenWithId(
                            currentAccount.getAccessToken(),
                            (int) currentAccount.getAccountId()
                    ),
                    currentGroup.getGroupId()
            );
            default -> LOGGER.error(String.format("Social media incorrect: %s", currentGroup.getSocialMedia()));
        }
        return groupName;
    }

    static String[] getButtonsForSyncOptions() {
        return new String[] {
                "Да",
                "yesNo 0",
                "Нет",
                "yesNo 1"
        };
    }
}
