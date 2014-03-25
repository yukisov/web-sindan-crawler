package net.pupha.wsc;

public abstract class Result {

    // 診断結果の種類
    enum ResultType {
        OK, NG,
    }
    // 診断結果
    private ResultType result = ResultType.NG;

    // 診断結果NGの場合の危険度の種類
    enum DangerLevelType {
        HIGH, MEDIUM, LOW,
    }
    // 診断結果NGの場合の危険度
    private DangerLevelType dangerLevel = DangerLevelType.MEDIUM;

    public Result(ResultType result, DangerLevelType dangerLevel) {
        setResult(result);
        setDangerLevel(dangerLevel);
    }

    abstract public void outputData();

    public ResultType getResult() {
        return result;
    }
    public void setResult(ResultType result) {
        this.result = result;
    }

    public DangerLevelType getDangerLevel() {
        return dangerLevel;
    }
    public void setDangerLevel(DangerLevelType dangerLevel) {
        this.dangerLevel = dangerLevel;
    }

}
