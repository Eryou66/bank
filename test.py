from modelscope.pipelines import pipeline
from modelscope.utils.constant import Tasks

clf = pipeline(Tasks.zero_shot_classification,
               model='damo/nlp_structbert_zero-shot-classification_chinese-base')

print(clf('我想查一下余额', candidate_labels=['余额查询', '转账', '挂失补卡', '投诉建议']))
