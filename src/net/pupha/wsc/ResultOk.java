package net.pupha.wsc;

public class ResultOk extends Result {

    public ResultOk() {
        super(Result.ResultType.OK, Result.DangerLevelType.LOW);
    }

    @Override
    public void outputData() {
        WSC.print("診断結果: ○ 正常");
    }
}
