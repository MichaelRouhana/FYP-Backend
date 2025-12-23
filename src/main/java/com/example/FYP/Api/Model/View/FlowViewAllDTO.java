package com.example.FYP.Api.Model.View;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowViewAllDTO {

    private String entity;
    private String mode;
    private boolean autoApprove;
    private List<FlowRuleDTO> rules;
    private List<FlowChainDTO> chains;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FlowChainDTO {
        private long id;
        private boolean defaultChain;
        private List<FlowStepDTO> steps;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlowStepDTO {
        private int index;
        private boolean end;
        private String tier;
        private String role;

        public static class FlowStepRuleDTO {
            private String key;
            private String type;
            private String condition;
            private String value;
            private String action;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlowRuleDTO {
        private String key;
        private String type;
        private String condition;
        private String value;
        private String action;
        private long chainId;
    }
}
