from modelscope.pipelines import pipeline
from modelscope.utils.constant import Tasks
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()
clf = pipeline(Tasks.zero_shot_classification,
               model='damo/nlp_structbert_zero-shot-classification_chinese-base')


class IntentReq(BaseModel):
    text: str
    labels: list[str]


@app.post('/intent')
def predict(req: IntentReq):
    result = clf(req.text, candidate_labels=req.labels)
    return result   # 返回结构先用第 2 步打印确认，通常是 labels + scores
