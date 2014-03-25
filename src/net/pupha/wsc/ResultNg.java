package net.pupha.wsc;

public class ResultNg extends Result {

    public ResultNg(DangerLevelType dangerLevel) {
        super(Result.ResultType.NG, dangerLevel);
    }

    @Override
    public void outputData() {
        WSC.print("診断結果: × 異常");
        switch (getDangerLevel()) {
        case HIGH:
            WSC.print("危険度: 高");
            break;
        case MEDIUM:
            WSC.print("危険度: 中");
            break;
        case LOW:
            WSC.print("危険度: 低");
            break;
        default:
            break;
        }
    }
}
