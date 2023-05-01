package polis.commands;

import polis.data.domain.CurrentAccount;
import polis.data.domain.CurrentGroup;
import polis.datacheck.OkDataCheck;
import polis.datacheck.VkDataCheck;
import polis.vk.api.VkAuthorizator;

public class CommandsUtils {
    static String getGroupName(CurrentAccount currentAccount, CurrentGroup currentGroup, OkDataCheck okDataCheck,
                               VkDataCheck vkDataCheck) {
        String groupName;
        switch (currentGroup.getSocialMedia()) {
            case OK -> groupName = okDataCheck.getOKGroupName(currentGroup.getGroupId(),
                    currentAccount.getAccessToken());
            case VK -> groupName = vkDataCheck.getVkGroupName(new VkAuthorizator.TokenWithId(
                            currentGroup.getAccessToken(), (int) currentAccount.getAccountId()
                    ),
                    String.valueOf(currentGroup.getGroupId())
            );
            default -> groupName = "";
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
