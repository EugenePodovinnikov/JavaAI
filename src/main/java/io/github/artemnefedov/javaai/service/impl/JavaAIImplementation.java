/*
 *  MIT License
 *
 *  Copyright (c)  2023. Artyom Nefedov
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package io.github.artemnefedov.javaai.service.impl;

import io.github.artemnefedov.javaai.dto.ImageBuilder;
import io.github.artemnefedov.javaai.dto.Chat;
import io.github.artemnefedov.javaai.dto.ChatMessage;
import io.github.artemnefedov.javaai.dto.Completions;
import io.github.artemnefedov.javaai.exception.JavaAIException;
import io.github.artemnefedov.javaai.service.JavaAI;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class for interacting with JavaAI implements the {@link JavaAI} interface.
 */
public class JavaAIImplementation implements JavaAI {

    private final URL completionsURL;
    private final URL imgBuilderURL;
    private final URL chatURL;

    private Completions completions;
    private final ImageBuilder imageBuilder = new ImageBuilder();
    private Chat chat;

    private final Connection connection;

    /**
     * Instantiates a new OpenAi implementation.
     *
     * @param apiKey the api key
     */
    public JavaAIImplementation(String apiKey) {
        this.connection = new Connection(apiKey);
        try {
            var baseURL = new URL("https://api.openai.com");
            this.completionsURL = new URL(baseURL + "/v1/completions");
            this.imgBuilderURL = new URL(baseURL + "/v1/images/generations");
            this.chatURL = new URL(baseURL + "/v1/chat/completions");
        } catch (MalformedURLException exception) {
            throw new JavaAIException(exception.getMessage());
        }
        defaultCompetitionsConfig();
        defaultChatConfig();
    }

    @Override
    public String generateText(String prompt) {
        this.completions.setPrompt(prompt);
        return connection
                .sendPost(this.completions, completionsURL, completions.getResponseModel())
                .getResponse();
    }

    @Override
    public String generateImage(String prompt) {
        this.imageBuilder.setPrompt(prompt);
        return connection
                .sendPost(this.imageBuilder, imgBuilderURL, imageBuilder.getResponseModel())
                .getResponse();

    }

    @Override
    public String chat(List<ChatMessage> messages) {
        messages.forEach(msg -> this.chat.getMessages().add(msg));
        var chatResponse = (Chat.ChatResponse) connection
                .sendPost(this.chat, chatURL, chat.getResponseModel());
        this.chat.getMessages().add(chatResponse.choices().get(0).message());
        return chatResponse.getResponse();
    }

    @Override
    public String chat(String userMessage) {
        this.chat.getMessages().add(new ChatMessage("user", userMessage));
        var chatResponse = (Chat.ChatResponse) connection
                .sendPost(this.chat, chatURL, chat.getResponseModel());
        this.chat.getMessages().add(chatResponse.choices().get(0).message());
        return chatResponse.getResponse();
    }

    @Override
    public void defaultCompetitionsConfig() {
        this.completions = Completions.builder()
                .model("text-davinci-003")
                .maxTokens(2000)
                .temperature(0.9f)
                .n((byte) 1)
                .bestOf(1)
                .build();
    }

    @Override
    public void defaultChatConfig() {
        this.chat = Chat.builder()
                .messages(new ArrayList<>())
                .model("gpt-3.5-turbo")
                .maxTokens(2000)
                .n(1)
                .build();
    }

    @Override
    public void setCompletions(Completions completions) {
        this.completions = completions;
    }

    @Override
    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
