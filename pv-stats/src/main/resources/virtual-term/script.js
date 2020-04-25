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

var prompt = {
    terminalElement: undefined,
    outputElement: undefined,
    promptElement: undefined,

    inputBuffer: "",

    init: function () {
        this.updatePrompt();
    },

    handleKeyDown: function (event) {
        var handled = true;
        switch (event.key) {
            case "Backspace":
                if (this.inputBuffer.length) {
                    this.inputBuffer = this.inputBuffer.substr(0, this.inputBuffer.length - 1);
                    this.updatePrompt();
                }
                break;
            case "Enter":
                this.sendCommand();
                break;
            default:
                handled = this.handleStandardKeyDown(event.key);
        }

        if (handled) {
            event.preventDefault();
        }
    },

    handleStandardKeyDown: function (key) {
        if (key.length > 1) {
            return false;
        }

        this.inputBuffer += key;
        this.updatePrompt();
        return true;
    },

    sendCommand: function () {
        var payload = {
          "command": this.inputBuffer
        };

        this.outputElement.textContent += ("\n" + PROMPT_CHAR + this.inputBuffer);
        this.inputBuffer = "";
        this.updatePrompt();

        var that = this;
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "command/execute");
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
        this.promptElement.textContent = PROMPT_CHAR + this.inputBuffer;
        this.terminalElement.scrollTo(0, this.terminalElement.scrollHeight);
    }
};
