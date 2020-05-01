function init() {
    var terminalElement = document.getElementById("terminal");

    prompt.terminalElement = terminalElement;
    prompt.outputElement = document.getElementById("output");
    prompt.promptElement = document.getElementById("prompt");
    prompt.init();

    terminalElement.onkeydown = function (ev) {
        prompt.handleKeyDown(ev);
    };
}

const PROMPT_CHAR = "> ";
const COMMAND_HISTORY_LIMIT = 15;

var prompt = {
    terminalElement: undefined,
    outputElement: undefined,
    promptElement: undefined,

    outputEmpty: true,
    inputBuffer: [""],
    commandHistory: [],
    commandHistoryPosition: 0,

    init: function () {
        this.promptElement.textContent = PROMPT_CHAR;
    },

    handleKeyDown: function (event) {
        var handled = true;
        switch (event.key) {
            case "Backspace":
                var pos = this.commandHistoryPosition;
                if (this.inputBuffer[pos].length) {

                    this.inputBuffer[pos] = this.inputBuffer[pos].substr(0, this.inputBuffer[pos].length - 1);
                    this.updatePrompt();
                }
                break;
            case "ArrowUp":
                this.viewHistoryItem(-1);
                break;
            case "ArrowDown":
                this.viewHistoryItem(1);
                break;
            case "Enter":
                var cmd = this.updateCommandHistory();
                if (!this.handleSpecialCommand(cmd)) {
                    this.sendCommand(cmd);
                }
                break;
            default:
                handled = this.handleStandardKeyDown(event.key);
        }

        if (handled) {
            event.preventDefault();
        }
    },

    viewHistoryItem: function (direction) {
        var targetPos = Math.max(0, Math.min(COMMAND_HISTORY_LIMIT, this.commandHistoryPosition + direction));
        if (targetPos < 0 || targetPos > COMMAND_HISTORY_LIMIT)
            return;

        this.commandHistoryPosition = targetPos;
        this.updatePrompt();
    },

    updateCommandHistory: function () {
        var newLineChar = "\n";
        if (this.outputEmpty) {
            newLineChar = "";
            this.outputEmpty = false;
        }

        this.outputElement.textContent += (newLineChar + PROMPT_CHAR + this.inputBuffer[this.commandHistoryPosition]);

        var newItem = this.inputBuffer[this.commandHistoryPosition];
        if (!newItem.length) {
            this.scrollToBottom();
            return newItem;
        }

        this.commandHistory.push(newItem);
        if (this.commandHistory.length > COMMAND_HISTORY_LIMIT)
            this.commandHistory.splice(0, this.commandHistory.length - COMMAND_HISTORY_LIMIT);

        this.commandHistoryPosition = Math.min(this.commandHistory.length, COMMAND_HISTORY_LIMIT);

        this.inputBuffer = [];
        for (var i = 0; i < this.commandHistory.length; i++)
            this.inputBuffer.push(this.commandHistory[i]);

        // Current command
        this.inputBuffer.push("");
        this.updatePrompt();
        return newItem;
    },

    handleStandardKeyDown: function (key) {
        if (key.length > 1) {
            return false;
        }

        this.inputBuffer[this.commandHistoryPosition] += key;
        this.updatePrompt();
        return true;
    },

    handleSpecialCommand: function (cmd) {
        cmd = cmd.toLowerCase().trim();
        switch (cmd) {
            case "cls":
            case "clear":
                this.outputElement.textContent = "";
                this.outputEmpty = true;
                return true;
            case "history":
                var append = "";
                for (var i = 0; i < this.commandHistory.length; i++) {
                    append += "\n";
                    append += (i + 1).toString().padStart(2, " ") + ". ";
                    append += this.commandHistory[i];
                }
                this.outputElement.textContent += append;
                this.scrollToBottom();
                return true;
        }

        return false;
    },

    sendCommand: function (cmd) {
        var payload = {
            "command": cmd
        };

        var that = this;
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "api/command/execute");
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(JSON.stringify(payload));
        xhr.onreadystatechange = function () {
            if (xhr.readyState !== XMLHttpRequest.DONE)
                return;

            var response = JSON.parse(xhr.responseText);
            var newContent = "";
            for (var i = 0; i < response.content.length; i++) {
                newContent += ("\n" + response.content[i]);
            }

            that.outputElement.textContent += newContent;
            that.terminalElement.scrollTo(0, that.terminalElement.scrollHeight + 50);
        };
    },

    updatePrompt: function () {
        this.promptElement.textContent = PROMPT_CHAR + this.inputBuffer[this.commandHistoryPosition];
        this.scrollToBottom();
    },

    scrollToBottom: function () {
        this.terminalElement.scrollTo(0, this.terminalElement.scrollHeight);
    }
};
