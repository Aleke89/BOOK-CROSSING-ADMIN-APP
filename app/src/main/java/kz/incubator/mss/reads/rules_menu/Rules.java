package kz.incubator.mss.reads.rules_menu;

public class Rules {
    String ruleId;
    String title;
    String desc;

    public Rules(){}

    public Rules(String ruleId, String title, String desc){
        this.ruleId = ruleId;
        this.title = title;
        this.desc = desc;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
