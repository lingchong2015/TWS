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
    private String datetime;

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

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * 1: 发射机正常.<br/>
     * 2: 发射机故障.<br/>
     * 3: 发射机停止.
     */
    public String[] getItemContent() {
        String nameContent = String.format("%s", name);

        if (status.equals("1")) {
            String frequencyContent = String.format("频率:%sMHZ", frequency);
            String transmissionPowerContent = String.format("发射功率:%sKW", transmission_power);
            String reflectionPowerContent = String.format("反射功率:%sW", reflection_power);
            String infoDatetime = String.format("接收时间:%s", datetime);
            String timeInfo = String.format("备注:%s", (note == null ? "" : note));

            return new String[]{nameContent, frequencyContent, transmissionPowerContent,
                    reflectionPowerContent, infoDatetime, timeInfo};
        } else if (status.equals("2")) {
            String infoDatetime = String.format("故障时间:%s", datetime);
            return new String[]{nameContent, infoDatetime,"报警信息:" + info};
        } else if (status.equals("3")) {
            String infoDatetime = String.format("停止时间:%s", datetime);
            return new String[]{nameContent, transmission_power, infoDatetime, "备注:" + (note == null ? "" : note)};
        } else {
            return new String[]{};
        }
    }
}
