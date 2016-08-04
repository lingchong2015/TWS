package curry.stephen.tws.model;

import java.util.UUID;

/**
 * Created by LingChong on 2016/6/22 0022.
 */
public class TransmitterDynamicInformationModel {

    private String name;
    private String frequency;
    private String transmission_power;
    private String reflection_power;
    private String status;
    private String note;
    private int id;
    private UUID mUUID;
    private String info;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getTransmission_power() {
        return transmission_power;
    }

    public void setTransmission_power(String transmission_power) {
        this.transmission_power = transmission_power;
    }

    public String getReflection_power() {
        return reflection_power;
    }

    public void setReflection_power(String reflection_power) {
        this.reflection_power = reflection_power;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public void setUUID(UUID UUID) {
        mUUID = UUID;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String[] getItemContent() {
        String nameContent = String.format("频道:%s", name);

        if (transmission_power.equals("数据暂无更新，请检查连接是否正常") ||
                transmission_power.equals("发射机处于关闭状态")) {
            return new String[]{nameContent, transmission_power, "备注:" + note};
        } else if (status.equals("1")) {
            return new String[]{nameContent, "报警信息:" + info};
        }

        String frequencyContent = String.format("频率:%s", frequency);
        String transmissionPowerContent = String.format("发射功率:%s", transmission_power);
        String reflectionPowerContent = String.format("反射功率:%s", reflection_power);
        String timeInfo = String.format("备注:%s", note);

        return new String[]{nameContent, frequencyContent, transmissionPowerContent,
                reflectionPowerContent, timeInfo};
    }
}
