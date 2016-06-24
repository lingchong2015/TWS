package curry.stephen.tws.model;

/**
 * Created by LingChong on 2016/6/22 0022.
 */
public class TransmitterDynamicInformationModel {

    private String name;
    private String frequency;
    private String transmission_power;
    private String reflection_power;

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

    public String[] getItemContent() {
        String nameContent = String.format("编号:%s", name);
        String frequencyContent = String.format("频率:%s", frequency);
        String transmissionPowerContent = String.format("发射功率:%s", transmission_power);
        String reflectionPowerContent = String.format("反射功率:%s", reflection_power);

        return new String[] {nameContent, frequencyContent, transmissionPowerContent,
                reflectionPowerContent};
    }
}
