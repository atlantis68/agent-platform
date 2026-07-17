package com.agentplatform.control.agent.controller;

import com.agentplatform.control.ControlPlaneApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ControlPlaneApplication.class)
@AutoConfigureMockMvc
class ToolRegistryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listToolsReturnsPhaseTwoDemoTools() throws Exception {
        mockMvc.perform(get("/api/v1/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tools[0].name").value("http_echo"))
                .andExpect(jsonPath("$.tools[0].riskLevel").value("LOW"))
                .andExpect(jsonPath("$.tools[0].inputSchema.type").value("object"))
                .andExpect(jsonPath("$.tools[1].name").value("mcp_local_time"))
                .andExpect(jsonPath("$.tools[1].sourceType").value("mcp"));
    }
}
