package com.steven.model.dto;


import com.steven.model.po.Todo;
import org.jboss.resteasy.reactive.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

public class TodoForm {

    public @FormParam("title") @PartType(MediaType.TEXT_PLAIN) String title;
    public @FormParam("completed") @PartType(MediaType.TEXT_PLAIN) String completed;
    public @FormParam("priority") @PartType(MediaType.TEXT_PLAIN) int priority;

    public Todo convertIntoTodo() {
        return new Todo(this);
    }

    public Todo updateTodo(Todo toUpdate) {
        return new Todo(toUpdate, this);
    }
}
